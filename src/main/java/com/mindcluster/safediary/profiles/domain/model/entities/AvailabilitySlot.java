package com.mindcluster.safediary.profiles.domain.model.entities;

import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Entity/component representing a bookable weekly recurring time slot.
 */
@Getter
public class AvailabilitySlot {

    private final DayOfWeek dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public AvailabilitySlot(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        if (dayOfWeek == null) {
            throw new IllegalArgumentException("Day of week must not be null");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start time and end time must not be null");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "End time (" + endTime + ") must be after start time (" + startTime + ") on " + dayOfWeek
            );
        }
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static AvailabilitySlot of(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        return new AvailabilitySlot(dayOfWeek, startTime, endTime);
    }

    /**
     * Checks if this slot overlaps with another slot.
     * Overlap occurs when both are on the same day of the week and their time intervals intersect.
     */
    public boolean overlapsWith(AvailabilitySlot other) {
        if (other == null || this.dayOfWeek != other.dayOfWeek) {
            return false;
        }
        // Slots overlap if: start < other.end AND other.start < end
        return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AvailabilitySlot that)) return false;
        return dayOfWeek == that.dayOfWeek &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayOfWeek, startTime, endTime);
    }

    @Override
    public String toString() {
        return dayOfWeek + " [" + startTime + " - " + endTime + "]";
    }
}
