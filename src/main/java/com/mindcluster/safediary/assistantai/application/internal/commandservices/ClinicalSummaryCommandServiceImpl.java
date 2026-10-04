package com.mindcluster.safediary.assistantai.application.internal.commandservices;

import com.mindcluster.safediary.assistantai.application.commandservices.ClinicalSummaryCommandService;
import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.AssistantLanguageModel;
import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.LlmSummaryRequest;
import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.LlmUnavailableException;
import com.mindcluster.safediary.assistantai.domain.model.aggregates.ClinicalSummary;
import com.mindcluster.safediary.assistantai.domain.model.commands.GenerateWeeklyClinicalSummaryCommand;
import com.mindcluster.safediary.assistantai.domain.model.entities.ConversationMessage;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.MessageSender;
import com.mindcluster.safediary.assistantai.domain.repositories.ClinicalSummaryRepository;
import com.mindcluster.safediary.assistantai.domain.repositories.ConversationSessionRepository;
import com.mindcluster.safediary.assistantai.domain.services.ClinicalSummarySynthesizerService;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;

/**
 * Clinical summary command service implementation.
 * <p>
 * Synthesizes the user's week without sending more than the last 60 messages to the language model.
 * TODO: once IAM exists, only include entries the patient authorized to share (Event Storming, flow 3).
 * </p>
 */
@Service
public class ClinicalSummaryCommandServiceImpl implements ClinicalSummaryCommandService {

    private static final int MAX_MESSAGES = 60;
    private static final int MAX_MESSAGE_LENGTH = 500;
    private static final int DOMINANT_EMOTIONS = 3;

    private final ConversationSessionRepository sessionRepository;
    private final ClinicalSummaryRepository clinicalSummaryRepository;
    private final AssistantLanguageModel languageModel;
    private final ClinicalSummarySynthesizerService synthesizer;
    private final ZoneId zone;

    public ClinicalSummaryCommandServiceImpl(ConversationSessionRepository sessionRepository,
                                             ClinicalSummaryRepository clinicalSummaryRepository,
                                             AssistantLanguageModel languageModel,
                                             ClinicalSummarySynthesizerService synthesizer,
                                             @Value("${assistantai.clinical-summary.zone:America/Lima}") String zoneId) {
        this.sessionRepository = sessionRepository;
        this.clinicalSummaryRepository = clinicalSummaryRepository;
        this.languageModel = languageModel;
        this.synthesizer = synthesizer;
        this.zone = ZoneId.of(zoneId);
    }

    @Override
    public Result<ClinicalSummary, ApplicationError> handle(GenerateWeeklyClinicalSummaryCommand command) {
        var accountId = command.accountId();
        if (accountId == null || accountId <= 0)
            return Result.failure(ApplicationError.validationError("accountId", "accountId must be a positive number"));
        var locale = "en".equalsIgnoreCase(command.locale()) ? "en" : "es";

        LocalDate weekStart;
        try {
            weekStart = synthesizer.resolveWeekStart(command.weekStart(), LocalDate.now(zone));
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("weekStart", ex.getMessage()));
        }
        var periodEnd = weekStart.plusDays(6);

        var existing = clinicalSummaryRepository.findByAccountIdAndPeriodStart(accountId, weekStart);
        if (existing.isPresent()) return Result.success(existing.get());

        var from = weekStart.atStartOfDay(zone).toInstant();
        var to = periodEnd.plusDays(1).atStartOfDay(zone).toInstant();
        var userMessages = sessionRepository.findAllByAccountIdOverlapping(accountId, from, to).stream()
                .flatMap(session -> session.getMessages().stream())
                .filter(message -> message.getSender() == MessageSender.USER)
                .filter(message -> !message.getSentAt().isBefore(from) && message.getSentAt().isBefore(to))
                .sorted(Comparator.comparing(ConversationMessage::getSentAt))
                .toList();
        if (userMessages.isEmpty())
            return Result.failure(ApplicationError.businessRuleViolation("clinical-summary-requires-messages",
                    "No user messages between " + weekStart + " and " + periodEnd));

        var dominantEmotions = synthesizer.dominantEmotions(userMessages, DOMINANT_EMOTIONS);
        var texts = userMessages.subList(Math.max(0, userMessages.size() - MAX_MESSAGES), userMessages.size()).stream()
                .map(ConversationMessage::getContent)
                .map(content -> content.length() > MAX_MESSAGE_LENGTH ? content.substring(0, MAX_MESSAGE_LENGTH) : content)
                .toList();

        try {
            var summary = languageModel.synthesizeWeeklySummary(new LlmSummaryRequest(locale, dominantEmotions, texts));
            var clinicalSummary = new ClinicalSummary(accountId, weekStart, periodEnd, dominantEmotions,
                    summary.keyTriggers(), summary.narrative(), summary.highlights());
            return Result.success(clinicalSummaryRepository.save(clinicalSummary));
        } catch (LlmUnavailableException ex) {
            return Result.failure(new ApplicationError("ASSISTANT_UNAVAILABLE",
                    "The AI assistant is temporarily unavailable", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.unexpected("clinical-summary", ex.getMessage()));
        }
    }
}
