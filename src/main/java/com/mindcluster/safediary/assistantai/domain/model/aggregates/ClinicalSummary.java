package com.mindcluster.safediary.assistantai.domain.model.aggregates;

import com.mindcluster.safediary.assistantai.domain.model.events.ClinicalSummaryGeneratedEvent;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PlutchikEmotionTag;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Weekly clinical summary aggregate root.
 * <p>
 * Synthesis of the user's week so a psychologist can prepare a session without reading raw transcripts.
 * </p>
 */
@Getter
public class ClinicalSummary extends AbstractDomainAggregateRoot<ClinicalSummary> {

    private Long id;
    private Long accountId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private List<PlutchikEmotionTag> dominantEmotions;
    private List<String> keyTriggers;
    private String synthesisNarrative;
    private List<String> highlights;
    private Instant generatedAt;

    /**
     * Creates a new summary and raises {@link ClinicalSummaryGeneratedEvent}.
     */
    public ClinicalSummary(Long accountId, LocalDate periodStart, LocalDate periodEnd,
                           List<PlutchikEmotionTag> dominantEmotions, List<String> keyTriggers,
                           String synthesisNarrative, List<String> highlights) {
        if (accountId == null || accountId <= 0)
            throw new IllegalArgumentException("accountId must be a positive number");
        if (periodStart == null || periodEnd == null || periodEnd.isBefore(periodStart))
            throw new IllegalArgumentException("invalid summary period");
        if (synthesisNarrative == null || synthesisNarrative.isBlank())
            throw new IllegalArgumentException("synthesis narrative must not be blank");
        this.accountId = accountId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.dominantEmotions = dominantEmotions == null ? List.of() : List.copyOf(dominantEmotions);
        this.keyTriggers = cleanTexts(keyTriggers);
        this.synthesisNarrative = synthesisNarrative.trim();
        this.highlights = cleanTexts(highlights);
        this.generatedAt = Instant.now();
        registerDomainEvent(new ClinicalSummaryGeneratedEvent(accountId, periodStart, periodEnd, generatedAt));
    }

    /**
     * Rebuilds a persisted summary. Only used by persistence assemblers.
     */
    public ClinicalSummary(Long id, Long accountId, LocalDate periodStart, LocalDate periodEnd,
                           List<PlutchikEmotionTag> dominantEmotions, List<String> keyTriggers,
                           String synthesisNarrative, List<String> highlights, Instant generatedAt) {
        this.id = id;
        this.accountId = accountId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.dominantEmotions = List.copyOf(dominantEmotions);
        this.keyTriggers = List.copyOf(keyTriggers);
        this.synthesisNarrative = synthesisNarrative;
        this.highlights = List.copyOf(highlights);
        this.generatedAt = generatedAt;
    }

    private static List<String> cleanTexts(List<String> values) {
        if (values == null) return List.of();
        return values.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(v -> v.replace('\n', ' ').trim())
                .toList();
    }
}
