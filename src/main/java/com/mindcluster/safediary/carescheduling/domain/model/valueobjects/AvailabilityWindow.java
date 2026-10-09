package com.mindcluster.safediary.carescheduling.domain.model.valueobjects;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** Weekly recurring bookable window in the clinician agenda timezone. */
public record AvailabilityWindow(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
    public AvailabilityWindow {
        if (dayOfWeek == null || startTime == null || endTime == null || !endTime.isAfter(startTime))
            throw new IllegalArgumentException("Each availability window needs a day and an end after its start");
    }
    public boolean overlaps(AvailabilityWindow other) {
        return dayOfWeek == other.dayOfWeek && startTime.isBefore(other.endTime) && endTime.isAfter(other.startTime);
    }
    public boolean contains(LocalTime start, LocalTime end) {
        return !start.isBefore(startTime) && !end.isAfter(endTime) && end.isAfter(start);
    }
}
