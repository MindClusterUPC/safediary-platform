package com.mindcluster.safediary.rutines.domain.model.commands;

public record SubmitDailyReflectionCommand(
        Long reflectionId,
        String answer
) {
    public SubmitDailyReflectionCommand {
        if (reflectionId == null) {
            throw new IllegalArgumentException("reflectionId is required");
        }
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("answer cannot be null or blank");
        }
    }
}