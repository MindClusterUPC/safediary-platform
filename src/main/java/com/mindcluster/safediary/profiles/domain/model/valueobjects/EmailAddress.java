package com.mindcluster.safediary.profiles.domain.model.valueobjects;

import java.util.regex.Pattern;

public record EmailAddress(String value) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email address must not be blank");
        }
        String normalized = value.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
        value = normalized;
    }

    public static EmailAddress of(String value) {
        return new EmailAddress(value);
    }
}
