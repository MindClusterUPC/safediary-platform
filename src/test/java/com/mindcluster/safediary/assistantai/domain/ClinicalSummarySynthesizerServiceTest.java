package com.mindcluster.safediary.assistantai.domain;

import com.mindcluster.safediary.assistantai.domain.model.entities.ConversationMessage;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.MessageSender;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PlutchikEmotionTag;
import com.mindcluster.safediary.assistantai.domain.services.ClinicalSummarySynthesizerService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static com.mindcluster.safediary.assistantai.domain.model.valueobjects.PlutchikEmotionTag.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClinicalSummarySynthesizerServiceTest {

    private final ClinicalSummarySynthesizerService service = new ClinicalSummarySynthesizerService();

    private static ConversationMessage userMessage(PlutchikEmotionTag tag) {
        return new ConversationMessage(null, MessageSender.USER, "texto", tag, Instant.now(), List.of());
    }

    @Test
    void dominantEmotionsAreOrderedByFrequency() {
        var messages = Stream.of(SADNESS, SADNESS, SADNESS, JOY, FEAR, FEAR, null)
                .map(ClinicalSummarySynthesizerServiceTest::userMessage)
                .toList();

        assertThat(service.dominantEmotions(messages, 2)).containsExactly(SADNESS, FEAR);
    }

    @Test
    void defaultWeekIsLastCompleteWeek() {
        assertThat(service.resolveWeekStart(null, LocalDate.of(2026, 10, 8))).isEqualTo(LocalDate.of(2026, 9, 28));
    }

    @Test
    void requestedWeekMustBeAPastMonday() {
        var today = LocalDate.of(2026, 10, 8);

        assertThatThrownBy(() -> service.resolveWeekStart(LocalDate.of(2026, 9, 29), today))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.resolveWeekStart(LocalDate.of(2026, 10, 12), today))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(service.resolveWeekStart(LocalDate.of(2026, 10, 5), today)).isEqualTo(LocalDate.of(2026, 10, 5));
    }
}
