package com.mindcluster.safediary.rutines.domain.model.events;

import java.time.LocalDateTime;

public record SosExerciseCompletedEvent(
        Long sosExerciseId,
        Long patientId,
        String exerciseType,
        Integer totalSteps,
        LocalDateTime completedAt
) {}