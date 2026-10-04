package com.mindcluster.safediary.assistantai.domain.services;

import com.mindcluster.safediary.assistantai.domain.model.entities.ConversationMessage;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PlutchikEmotionTag;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Domain rules used to build weekly clinical summaries.
 */
public class ClinicalSummarySynthesizerService {

    /**
     * Most frequent emotions in the user messages; ties are resolved by enum order.
     */
    public List<PlutchikEmotionTag> dominantEmotions(List<ConversationMessage> userMessages, int top) {
        return userMessages.stream()
                .map(ConversationMessage::getEmotionTag)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(),
                        () -> new EnumMap<>(PlutchikEmotionTag.class), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<PlutchikEmotionTag, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(top)
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Without a requested week, returns the Monday of the last complete week.
     * A requested week must start on a Monday and cannot be in the future.
     */
    public LocalDate resolveWeekStart(LocalDate requested, LocalDate today) {
        if (requested == null)
            return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
        if (requested.getDayOfWeek() != DayOfWeek.MONDAY)
            throw new IllegalArgumentException("weekStart must be a Monday");
        if (requested.isAfter(today))
            throw new IllegalArgumentException("weekStart cannot be in the future");
        return requested;
    }
}
