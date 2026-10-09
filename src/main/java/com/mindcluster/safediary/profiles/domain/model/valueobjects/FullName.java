package com.mindcluster.safediary.profiles.domain.model.valueobjects;

public record FullName(String firstName, String lastName) {
    public FullName {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name must not be blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name must not be blank");
        }
        firstName = firstName.trim();
        lastName = lastName.trim();
    }

    public static FullName of(String firstName, String lastName) {
        return new FullName(firstName, lastName);
    }

    public static FullName parse(String fullNameStr) {
        if (fullNameStr == null || fullNameStr.isBlank()) {
            throw new IllegalArgumentException("Full name must not be blank");
        }
        String[] parts = fullNameStr.trim().split("\\s+", 2);
        if (parts.length == 1) {
            return new FullName(parts[0], "");
        }
        return new FullName(parts[0], parts[1]);
    }

    public String toCombinedString() {
        if (lastName.isBlank()) {
            return firstName;
        }
        return firstName + " " + lastName;
    }
}
