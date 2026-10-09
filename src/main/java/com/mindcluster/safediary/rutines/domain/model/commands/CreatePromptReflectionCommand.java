package com.mindcluster.safediary.rutines.domain.model.commands;

public record CreatePromptReflectionCommand(
        Long patientId,
        String promptText
) {
    public CreatePromptReflectionCommand {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId is required");
        }
        if (promptText == null || promptText.isBlank()) {
            throw new IllegalArgumentException("promptText is required");
        }
    }
}
