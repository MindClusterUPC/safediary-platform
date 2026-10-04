package com.mindcluster.safediary.assistantai.domain;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ClinicalSummary;
import com.mindcluster.safediary.assistantai.domain.model.events.ClinicalSummaryGeneratedEvent;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PlutchikEmotionTag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClinicalSummaryTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 9, 28);

    @Test
    void validSummaryRaisesEventAndCleansTexts() {
        var summary = new ClinicalSummary(1L, MONDAY, MONDAY.plusDays(6), List.of(PlutchikEmotionTag.SADNESS),
                List.of("Exámenes\nfinales", " "), "El paciente tuvo una semana exigente.", null);

        assertThat(summary.getKeyTriggers()).containsExactly("Exámenes finales");
        assertThat(summary.getHighlights()).isEmpty();
        assertThat(summary.domainEvents()).hasAtLeastOneElementOfType(ClinicalSummaryGeneratedEvent.class);
    }

    @Test
    void rejectsBlankNarrative() {
        assertThatThrownBy(() -> new ClinicalSummary(1L, MONDAY, MONDAY.plusDays(6), List.of(), List.of(), " ", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsPeriodEndBeforeStart() {
        assertThatThrownBy(() -> new ClinicalSummary(1L, MONDAY, MONDAY.minusDays(1), List.of(), List.of(), "ok", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
