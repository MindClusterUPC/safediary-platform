package com.mindcluster.safediary.profiles.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record ConfigureAvailabilityResource(
        @NotNull
        @Schema(description = "List of weekly recurring availability slots")
        List<@Valid AvailabilitySlotResource> slots
) {
    public record AvailabilitySlotResource(
            @NotNull
            @Schema(description = "Day of the week", example = "MONDAY")
            DayOfWeek dayOfWeek,

            @NotNull
            @Schema(description = "Start time (HH:mm)", example = "09:00:00")
            LocalTime startTime,

            @NotNull
            @Schema(description = "End time (HH:mm)", example = "13:00:00")
            LocalTime endTime
    ) {}
}
