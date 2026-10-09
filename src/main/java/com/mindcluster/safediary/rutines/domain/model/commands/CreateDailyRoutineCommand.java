package com.mindcluster.safediary.rutines.domain.model.commands;

import com.mindcluster.safediary.rutines.domain.model.valueobjects.FrequencyDays;

import java.util.Set;

public record CreateDailyRoutineCommand(
        Long patientId,
        String title,
        Set<FrequencyDays> frequencyDays,
        boolean isNotificationActive
) {
    public CreateDailyRoutineCommand {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId is required");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
        if (frequencyDays == null || frequencyDays.isEmpty()) {
            throw new IllegalArgumentException("frequencyDays cannot be null or empty");
        }
    }
}