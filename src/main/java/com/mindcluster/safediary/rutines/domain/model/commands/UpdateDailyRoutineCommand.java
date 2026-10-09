package com.mindcluster.safediary.rutines.domain.model.commands;

public record UpdateDailyRoutineCommand(
        Long routineId,
        String title,
        boolean isNotificationActive
) {
    public UpdateDailyRoutineCommand {
        if (routineId == null) {
            throw new IllegalArgumentException("routineId is required");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
    }
}