package com.mindcluster.safediary.rutines.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePromptReflectionResource(
        @NotNull
        @Schema(description = "Patient ID to assign the prompt reflection to", example = "1")
        Long patientId,

        @NotBlank
        @Size(max = 500)
        @Schema(description = "Reflection question text", example = "¿Qué momento del día te brindó mayor sensación de calma y por qué?")
        String promptText
) {
    public CreatePromptReflectionResource {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId is required");
        }
        if (promptText == null || promptText.isBlank()) {
            throw new IllegalArgumentException("promptText is required");
        }
    }
}
