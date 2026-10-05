package com.mindcluster.safediary.assistantai.application.internal.eventhandlers;

import com.mindcluster.safediary.assistantai.application.commandservices.ClinicalSummaryCommandService;
import com.mindcluster.safediary.assistantai.domain.model.commands.GenerateWeeklyClinicalSummaryCommand;
import com.mindcluster.safediary.assistantai.domain.repositories.ConversationSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

/**
 * Consolidates the weekly clinical summary of every account with conversations at the end of each week.
 */
@Component
@ConditionalOnProperty(name = "assistantai.clinical-summary.scheduler.enabled", havingValue = "true")
public class WeeklySummaryTriggerEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeeklySummaryTriggerEventHandler.class);

    private final ClinicalSummaryCommandService clinicalSummaryCommandService;
    private final ConversationSessionRepository sessionRepository;
    private final ZoneId zone;

    public WeeklySummaryTriggerEventHandler(ClinicalSummaryCommandService clinicalSummaryCommandService,
                                            ConversationSessionRepository sessionRepository,
                                            @Value("${assistantai.clinical-summary.zone:America/Lima}") String zoneId) {
        this.clinicalSummaryCommandService = clinicalSummaryCommandService;
        this.sessionRepository = sessionRepository;
        this.zone = ZoneId.of(zoneId);
    }

    @Scheduled(cron = "${assistantai.clinical-summary.cron:0 0 23 * * SUN}",
               zone = "${assistantai.clinical-summary.zone:America/Lima}")
    public void generateWeeklySummaries() {
        var weekStart = LocalDate.now(zone).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        var from = weekStart.atStartOfDay(zone).toInstant();
        var to = weekStart.plusDays(7).atStartOfDay(zone).toInstant();
        for (var accountId : sessionRepository.findAccountIdsWithSessionsOverlapping(from, to)) {
            var result = clinicalSummaryCommandService.handle(
                    new GenerateWeeklyClinicalSummaryCommand(accountId, weekStart, "es"));
            if (result.isFailure())
                LOGGER.warn("Weekly clinical summary not generated for account {}", accountId);
        }
    }
}
