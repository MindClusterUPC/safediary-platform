package com.mindcluster.safediary.profiles.domain.model.commands;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record ConfigurePsychologistAvailabilityCommand(
        Long psychologistId,
        List<AvailabilitySlotEntry> slots
) {
    public record AvailabilitySlotEntry(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {}
}
