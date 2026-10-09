package com.mindcluster.safediary.rutines.interfaces.rest.resources;

import java.time.LocalDateTime;

public record SosExerciseResponseResource(
        Long id,
        Long patientId,
        String exerciseType,
        Integer currentStep,
        Integer totalSteps,
        LocalDateTime completedAt
) {}