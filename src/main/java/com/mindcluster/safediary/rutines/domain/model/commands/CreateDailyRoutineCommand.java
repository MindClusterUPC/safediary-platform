package com.mindcluster.safediary.rutines.domain.model.commands;

public record CreateDailyRoutineCommand(
        String title,
        Set<DayOfWeek> frequencyDays,
        boolean isNotificationActive,
        Integer targetHour,
        Integer targetMinute
) {}