package com.mindcluster.safediary.rutines.domain.model.queries;

public record GetPromptReflectionByIdQuery(Long reflectionId) {
    public GetPromptReflectionByIdQuery {
        if (reflectionId == null) {
            throw new IllegalArgumentException("reflectionId is required");
        }
    }
}
