package com.mindcluster.safediary.rutines.domain.model.commands;

import com.mindcluster.safediary.rutines.domain.model.valueobjects.RoutineDetails;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.FrequencyDays;
import java.util.Set;

public record UpdateDailyRoutineCommand(
        Long routineId,
        String title,
        boolean isNotificationActive,
        Set<FrequencyDays> frequencyDays,
        RoutineDetails details
) {
    public UpdateDailyRoutineCommand(Long id, String title, boolean notify) {
        this(id, title, notify, null, null);
    }
    public UpdateDailyRoutineCommand {
        if (routineId == null) {
            throw new IllegalArgumentException("routineId is required");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title is required");
        }
    }
}
