package com.mindcluster.safediary.rutines.domain.model.valueobjects;

#Valida si la respuesta de la reflexion cumple con los parametros

public record ReflectionAnswer(String answer) {
    public ReflectionAnswer {
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("Reflection answer cannot be null or blank");
        }   
        if (answer.length() > 500) {
            throw new IllegalArgumentException("Reflection answer cannot exceed 500 characters");
        }
    }

    public static ReflectionAnswer of(String answer) {
        return new ReflectionAnswer(answer);
    }
}