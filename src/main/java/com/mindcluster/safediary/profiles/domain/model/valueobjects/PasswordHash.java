package com.mindcluster.safediary.profiles.domain.model.valueobjects;

public record PasswordHash(String value) {
    public PasswordHash {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        if (value.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        value = value.trim();
    }

    public static PasswordHash of(String value) {
        return new PasswordHash(value);
    }
}
