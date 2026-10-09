package com.mindcluster.safediary.rutines.interfaces.rest.resources;

public record PromptReflectionResponseResource(
        Long id,
        Long patientId,
        String promptText,
        String answer,
        String status
) {}