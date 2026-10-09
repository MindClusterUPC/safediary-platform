package com.mindcluster.safediary.rutines.domain.model.commands;

import com.mindcluster.safediary.rutines.domain.model.valueobjects.ExerciseType;

public record StartSosExerciseCommand(
        Long patientId,
        ExerciseType type,
        Integer totalSteps
) {
    public StartSosExerciseCommand {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("type is required");
        }
        if (totalSteps == null || totalSteps < 1) {
            throw new IllegalArgumentException("totalSteps must be at least 1");
        }
    }

    public StartSosExerciseCommand(Long patientId, String exerciseType, Integer totalSteps) {
        this(patientId, ExerciseType.of(exerciseType), totalSteps);
    }
}