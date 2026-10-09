package com.mindcluster.safediary.rutines.domain.model.events;

import java.util.Set;

public record DailyRoutineCreatedEvent(
        Long routineId,
        Long patientId, 
        String title,
        Set<String> frequencyDays,
        boolean isNotificationActive
) {}