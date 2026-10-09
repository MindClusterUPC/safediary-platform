package com.mindcluster.safediary.carescheduling.infrastructure.acl;

import com.mindcluster.safediary.assistantai.application.queryservices.ClinicalSummaryQueryService;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetWeeklyClinicalSummaryQuery;
import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.summaries.EmotionalSummaryDto;
import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.summaries.EmotionalSummaryClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Anticorruption layer towards AssistantAI (Customer/Supplier + ACL). It reads only the latest weekly summary
 * through the public query service and translates it; conversations never reach Care Scheduling.
 */
@Component @RequiredArgsConstructor
public class AssistantAiEmotionalSummaryClient implements EmotionalSummaryClient {
    private final ClinicalSummaryQueryService summaries;

    public Optional<EmotionalSummaryDto> fetchLatestSummary(Long patientAccountId) {
        return summaries.handle(new GetWeeklyClinicalSummaryQuery(patientAccountId, null)).map(s -> new EmotionalSummaryDto(
                s.getPeriodStart(), s.getPeriodEnd(), s.getDominantEmotions().stream().map(Enum::name).toList(),
                s.getKeyTriggers(), s.getSynthesisNarrative(), s.getHighlights()));
    }
}
