package com.mindcluster.safediary.rutines.domain.model.commands;

public record ToggleDailyRoutineActiveCommand(
        Long routineId
) {
    public ToggleDailyRoutineActiveCommand {
        if (routineId == null) {
            throw new IllegalArgumentException("routineId is required");
        }
    }
}
