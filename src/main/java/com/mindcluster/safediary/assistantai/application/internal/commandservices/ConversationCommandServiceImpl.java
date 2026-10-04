package com.mindcluster.safediary.assistantai.application.internal.commandservices;

import com.mindcluster.safediary.assistantai.application.commandservices.ConversationCommandService;
import com.mindcluster.safediary.assistantai.application.commandservices.ReflectionResult;
import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.AssistantLanguageModel;
import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.LlmDistortion;
import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.LlmReflectionRequest;
import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.LlmUnavailableException;
import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;
import com.mindcluster.safediary.assistantai.domain.model.commands.*;
import com.mindcluster.safediary.assistantai.domain.repositories.ConversationSessionRepository;
import com.mindcluster.safediary.assistantai.domain.services.CognitiveDistortionService;
import com.mindcluster.safediary.assistantai.domain.services.EmotionClassifierService;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Conversation command service implementation.
 * <p>
 * Orchestrates the AssistantAI storytelling: message received, emotion classified, distortions detected
 * and reflection generated.
 * It is intentionally not transactional: the language model is never called inside a transaction.
 * </p>
 */
@Service
public class ConversationCommandServiceImpl implements ConversationCommandService {

    private final ConversationSessionRepository sessionRepository;
    private final AssistantLanguageModel languageModel;
    private final EmotionClassifierService emotionClassifierService;
    private final CognitiveDistortionService cognitiveDistortionService;
    private final int historyLimit;

    public ConversationCommandServiceImpl(ConversationSessionRepository sessionRepository,
                                          AssistantLanguageModel languageModel,
                                          EmotionClassifierService emotionClassifierService,
                                          CognitiveDistortionService cognitiveDistortionService,
                                          @Value("${assistantai.llm.history-limit:20}") int historyLimit) {
        this.sessionRepository = sessionRepository;
        this.languageModel = languageModel;
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

        try {
            var reply = languageModel.generateReflection(
                    new LlmReflectionRequest(session.getCurrentTone(), locale, session.getRecentMessages(historyLimit)));
            session.classifyLastUserMessage(emotionClassifierService.normalizeToPlutchik(reply.emotion()));
            var distortions = (reply.distortions() == null ? List.<LlmDistortion>of() : reply.distortions()).stream()
                    .map(d -> cognitiveDistortionService.toReliableDistortion(d.type(), d.evidence(), d.confidence()))
                    .flatMap(Optional::stream)
                    .toList();
            session.recordDistortionsOnLastUserMessage(distortions);
            session.addAssistantReflection(reply.reply());
        } catch (LlmUnavailableException ex) {
            return Result.failure(new ApplicationError("ASSISTANT_UNAVAILABLE",
                    "The AI assistant is temporarily unavailable", ex.getMessage()));
        }

        var saved = sessionRepository.save(session);
        var messages = saved.getMessages();
        return Result.success(new ReflectionResult(saved, messages.get(messages.size() - 2),
                messages.get(messages.size() - 1)));
    }

    @Override
    public Result<ConversationSession, ApplicationError> handle(ChangePersonalityToneCommand command) {
        return applyToSession(command.sessionId(), session -> session.changeTone(command.tone()));
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
