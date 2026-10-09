package com.mindcluster.safediary.rutines.domain.model.commands;

public record ToggleRoutineNotificationCommand(
        Long routineId,
        boolean isEnabled
) {
    public ToggleRoutineNotificationCommand {
        if (routineId == null) {
            throw new IllegalArgumentException("routineId is required");
        }
    }
}