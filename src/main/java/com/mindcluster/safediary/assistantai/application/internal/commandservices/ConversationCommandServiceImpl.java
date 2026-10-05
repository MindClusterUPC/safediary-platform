package com.mindcluster.safediary.assistantai.application.internal.commandservices;

import com.mindcluster.safediary.assistantai.application.commandservices.ConversationCommandService;
import com.mindcluster.safediary.assistantai.application.commandservices.ReflectionResult;
import com.mindcluster.safediary.assistantai.application.internal.outboundservices.crisis.CrisisHotlineDirectory;
import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.AssistantLanguageModel;
import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.LlmDistortion;
import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.LlmReflectionRequest;
import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.LlmUnavailableException;
import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;
import com.mindcluster.safediary.assistantai.domain.model.aggregates.RiskAssessment;
import com.mindcluster.safediary.assistantai.domain.model.commands.*;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.CrisisHotline;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.RiskEvaluation;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.SessionStatus;
import com.mindcluster.safediary.assistantai.domain.repositories.ConversationSessionRepository;
import com.mindcluster.safediary.assistantai.domain.repositories.RiskAssessmentRepository;
import com.mindcluster.safediary.assistantai.domain.services.CognitiveDistortionService;
import com.mindcluster.safediary.assistantai.domain.services.EmotionClassifierService;
import com.mindcluster.safediary.assistantai.domain.services.RiskPolicyService;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Conversation command service implementation.
 * <p>
 * Orchestrates the AssistantAI storytelling: message received, emotion classified, distortions detected,
 * risk evaluated, crisis protocol (if high or critical) and reflection generated.
 * It is intentionally not transactional: the language model is never called inside a transaction.
 * </p>
 */
@Service
public class ConversationCommandServiceImpl implements ConversationCommandService {

    static final String CRISIS_SAFE_MESSAGE_ES =
            "Lo que me cuentas es muy importante y mereces apoyo ahora mismo. No tienes que pasar por esto en soledad. "
                    + "Por favor comunícate de inmediato con la Línea 113, opción 5 (salud mental, gratuita, 24 horas) o con el SAMU al 106. "
                    + "Si estás en peligro inmediato, acude a la emergencia más cercana o pide ayuda a alguien de confianza.";
    static final String CRISIS_SAFE_MESSAGE_EN =
            "What you are sharing is very important and you deserve support right now. You don't have to go through this alone. "
                    + "Please contact Línea 113, option 5 (mental health, free, 24 hours) or SAMU at 106 right away. "
                    + "If you are in immediate danger, go to the nearest emergency room or reach out to someone you trust.";

    private final ConversationSessionRepository sessionRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final AssistantLanguageModel languageModel;
    private final CrisisHotlineDirectory crisisHotlineDirectory;
    private final RiskPolicyService riskPolicyService;
    private final EmotionClassifierService emotionClassifierService;
    private final CognitiveDistortionService cognitiveDistortionService;
    private final int historyLimit;

    public ConversationCommandServiceImpl(ConversationSessionRepository sessionRepository,
                                          RiskAssessmentRepository riskAssessmentRepository,
                                          AssistantLanguageModel languageModel,
                                          CrisisHotlineDirectory crisisHotlineDirectory,
                                          RiskPolicyService riskPolicyService,
                                          EmotionClassifierService emotionClassifierService,
                                          CognitiveDistortionService cognitiveDistortionService,
                                          @Value("${assistantai.llm.history-limit:20}") int historyLimit) {
        this.sessionRepository = sessionRepository;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.languageModel = languageModel;
        this.crisisHotlineDirectory = crisisHotlineDirectory;
        this.riskPolicyService = riskPolicyService;
        this.emotionClassifierService = emotionClassifierService;
        this.cognitiveDistortionService = cognitiveDistortionService;
        this.historyLimit = historyLimit;
    }

    @Override
    public Result<ConversationSession, ApplicationError> handle(StartConversationCommand command) {
        var active = command.accountId() == null ? Optional.<ConversationSession>empty()
                : sessionRepository.findActiveByAccountId(command.accountId());
        if (active.isPresent())
            return Result.failure(ApplicationError.conflict("ConversationSession",
                    "Account already has an active session: " + active.get().getId()));
        try {
            return Result.success(sessionRepository.save(new ConversationSession(command.accountId(), command.tone())));
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("accountId", ex.getMessage()));
        }
    }

    @Override
    public Result<ReflectionResult, ApplicationError> handle(SendTextMessageCommand command) {
        var locale = "en".equalsIgnoreCase(command.locale()) ? "en" : "es";
        var crisisMessage = locale.equals("en") ? CRISIS_SAFE_MESSAGE_EN : CRISIS_SAFE_MESSAGE_ES;

        var found = sessionRepository.findById(command.sessionId());
        if (found.isEmpty())
            return Result.failure(ApplicationError.notFound("ConversationSession", String.valueOf(command.sessionId())));
        var session = found.get();

        try {
            session.receiveUserMessage(command.content());
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("content", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return Result.failure(ApplicationError.businessRuleViolation("session-not-closed", ex.getMessage()));
        }

        RiskEvaluation finalRisk = riskPolicyService.evaluate(command.content(), null);
        String replyText;
        if (riskPolicyService.requiresCrisisProtocol(finalRisk)) {
            replyText = crisisMessage;
        } else {
            try {
                var reply = languageModel.generateReflection(
                        new LlmReflectionRequest(session.getCurrentTone(), locale, session.getRecentMessages(historyLimit)));
                session.classifyLastUserMessage(emotionClassifierService.normalizeToPlutchik(reply.emotion()));
                var distortions = (reply.distortions() == null ? List.<LlmDistortion>of() : reply.distortions()).stream()
                        .map(d -> cognitiveDistortionService.toReliableDistortion(d.type(), d.evidence(), d.confidence()))
                        .flatMap(Optional::stream)
                        .toList();
                session.recordDistortionsOnLastUserMessage(distortions);
                finalRisk = riskPolicyService.evaluate(command.content(), reply.riskScore());
                replyText = riskPolicyService.requiresCrisisProtocol(finalRisk) ? crisisMessage : reply.reply();
            } catch (LlmUnavailableException ex) {
                return Result.failure(new ApplicationError("ASSISTANT_UNAVAILABLE",
                        "The AI assistant is temporarily unavailable", ex.getMessage()));
            }
        }

        if (riskPolicyService.requiresCrisisProtocol(finalRisk)) session.flagForCrisis();
        session.addAssistantReflection(replyText);
        var saved = sessionRepository.save(session);
        riskAssessmentRepository.save(new RiskAssessment(saved.getId(), finalRisk));

        var messages = saved.getMessages();
        var crisisResources = saved.isInCrisis() ? crisisHotlineDirectory.findAll() : List.<CrisisHotline>of();
        return Result.success(new ReflectionResult(saved, messages.get(messages.size() - 2),
                messages.get(messages.size() - 1), finalRisk.level(), crisisResources));
    }

    @Override
    public Result<ReflectionResult, ApplicationError> handle(SendChatPromptCommand command) {
        var conversationId = command.conversationId();
        var session = Optional.<ConversationSession>empty();
        if (conversationId != null && conversationId.matches("\\d+")) {
            session = sessionRepository.findById(Long.valueOf(conversationId))
                    .filter(s -> s.getAccountId().equals(command.accountId()))
                    .filter(s -> s.getStatus() != SessionStatus.CLOSED);
        }
        var resolved = session.orElseGet(() -> sessionRepository.save(new ConversationSession(command.accountId(),
                command.tone() != null ? command.tone() : PersonalityTone.EMPATHIC)));
        if (command.tone() != null && command.tone() != resolved.getCurrentTone()) {
            resolved.changeTone(command.tone());
            resolved = sessionRepository.save(resolved);
        }
        return handle(new SendTextMessageCommand(resolved.getId(), command.prompt(), command.locale()));
    }

    @Override
    public Result<ConversationSession, ApplicationError> handle(ChangePersonalityToneCommand command) {
        return applyToSession(command.sessionId(), session -> session.changeTone(command.tone()));
    }

    @Override
    public Result<ConversationSession, ApplicationError> handle(RenameConversationCommand command) {
        var found = sessionRepository.findById(command.conversationId());
        if (found.isEmpty() || (command.accountId() != null && !found.get().getAccountId().equals(command.accountId()))) {
            return Result.failure(ApplicationError.notFound("Conversation", String.valueOf(command.conversationId())));
        }
        var session = found.get();
        try {
            session.rename(command.title());
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("title", ex.getMessage()));
        }
        var saved = sessionRepository.save(session);
        return Result.success(saved);
    }

    @Override
    @Transactional
    public Result<Void, ApplicationError> handle(DeleteConversationCommand command) {
        var found = sessionRepository.findById(command.conversationId());
        if (found.isEmpty() || (command.accountId() != null && !found.get().getAccountId().equals(command.accountId()))) {
            return Result.failure(ApplicationError.notFound("Conversation", String.valueOf(command.conversationId())));
        }
        riskAssessmentRepository.deleteAllBySessionId(command.conversationId());
        sessionRepository.delete(found.get());
        return Result.success(null);
    }

    @Override
    public Result<ReflectionResult, ApplicationError> handle(EditUserMessageCommand command) {
        var found = sessionRepository.findById(command.conversationId());
        if (found.isEmpty() || (command.accountId() != null && !found.get().getAccountId().equals(command.accountId()))) {
            return Result.failure(ApplicationError.notFound("Conversation", String.valueOf(command.conversationId())));
        }
        var session = found.get();
        if (command.tone() != null && command.tone() != session.getCurrentTone()) {
            session.changeTone(command.tone());
        }
        try {
            session.truncateFrom(command.messageId());
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("messageId", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return Result.failure(ApplicationError.businessRuleViolation("session-not-closed", ex.getMessage()));
        }
        sessionRepository.save(session);
        return handle(new SendTextMessageCommand(session.getId(), command.prompt(), command.locale()));
    }

    @Override
    public Result<ConversationSession, ApplicationError> handle(CloseConversationCommand command) {
        return applyToSession(command.sessionId(), ConversationSession::close);
    }

    private Result<ConversationSession, ApplicationError> applyToSession(Long sessionId, Consumer<ConversationSession> action) {
        var found = sessionRepository.findById(sessionId);
        if (found.isEmpty())
            return Result.failure(ApplicationError.notFound("ConversationSession", String.valueOf(sessionId)));
        var session = found.get();
        try {
            action.accept(session);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("tone", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return Result.failure(ApplicationError.businessRuleViolation("session-not-closed", ex.getMessage()));
        }
        return Result.success(sessionRepository.save(session));
    }
}
