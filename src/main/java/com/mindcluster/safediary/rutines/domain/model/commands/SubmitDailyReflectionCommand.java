package com.mindcluster.safediary.rutines.domain.model.commands;

public record SubmitDailyReflectionCommand(
        Long reflectionId,
        String answer
) {}