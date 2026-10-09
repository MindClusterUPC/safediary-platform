package com.mindcluster.safediary.rutines.domain.model.valueobjects;

public record PromptText(String value) {
    public PromptText {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Prompt text cannot be null or blank");
        }
        if (value.length() > 500) {
            throw new IllegalArgumentException("Prompt text cannot exceed 500 characters");
        }
    }

    public static PromptText of(String value) {
        return new PromptText(value);
    }
}
