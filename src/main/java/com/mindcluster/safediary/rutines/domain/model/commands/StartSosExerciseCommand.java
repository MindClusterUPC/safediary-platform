package com.mindcluster.safediary.rutines.domain.model.commands;

import com.mindcluster.safediary.rutines.domain.model.valueobjects.ExerciseType;

public record StartSosExerciseCommand(
        Long patientId,
        ExerciseType type,
        Integer totalSteps
) {}