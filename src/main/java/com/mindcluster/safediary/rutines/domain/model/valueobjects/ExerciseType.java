package com.mindcluster.safediary.rutines.domain.model.valueobjects;

public record ExerciseType(String value) {
    public ExerciseType {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Exercise type cannot be null or blank");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("Exercise type cannot exceed 100 characters");
        }
    }

    public static ExerciseType of(String value) {
        return new ExerciseType(value);
    }
}