package com.mindcluster.safediary.rutines.domain.model.commands;

public record ToggleRoutineNotificationCommand(
        Long routineId,
        boolean isEnabled
) {}