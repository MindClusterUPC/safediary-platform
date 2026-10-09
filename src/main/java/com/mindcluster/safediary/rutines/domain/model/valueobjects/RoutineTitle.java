package com.mindcluster.safediary.rutines.domain.model.valueobjects;

public record RoutineTitle(String value) {

    public RoutineTitle {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Routine title cannot be null or blank");
        }
        else if (value.length() > 60) {
            throw new IllegalArgumentException("Routine title cannot exceed 60 characters");
        }
    }

    public static RoutineTitle of(String value) {
        return new RoutineTitle(value);
    }
}