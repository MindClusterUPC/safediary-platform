package com.mindcluster.safediary.rutines.domain.model.valueobjects;

public record ReflectionAnswer(String answer) {
    public ReflectionAnswer {
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("Reflection answer cannot be null or blank");
        }
        if (answer.length() > 2000) {
            throw new IllegalArgumentException("Reflection answer cannot exceed 2000 characters");
        }
    }

    public static ReflectionAnswer of(String answer) {
        return new ReflectionAnswer(answer);
    }

    public String value() {
        return answer;
    }
}