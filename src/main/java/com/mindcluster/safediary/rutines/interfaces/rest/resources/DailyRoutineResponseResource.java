package com.mindcluster.safediary.rutines.interfaces.rest.resources;

import java.time.LocalDate;

import java.util.Set;

public record DailyRoutineResponseResource(
        Long id,
        Long patientId,
        String title,
        Set<String> frequencyDays,
        String notificationStatus,
        boolean isActive,
        String description,
        String time,
        Integer minutes,
        String kind,
        LocalDate completedOn
) {}
