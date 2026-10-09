package com.mindcluster.safediary.rutines.domain.model.commands;

public record AdvanceSosStepCommand(Long sosExerciseId) {
    public AdvanceSosStepCommand {
        if (sosExerciseId == null) {
            throw new IllegalArgumentException("sosExerciseId is required");
        }
    }
}