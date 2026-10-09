package com.mindcluster.safediary.rutines.domain.model.commands;

import java.time.DayOfWeek;
import java.util.Set;

public record UpdateDailyRoutineCommand(
        Long routineId,
        String title,
        Set<DayOfWeek> frequencyDays,
        boolean isNotificationActive
) {}