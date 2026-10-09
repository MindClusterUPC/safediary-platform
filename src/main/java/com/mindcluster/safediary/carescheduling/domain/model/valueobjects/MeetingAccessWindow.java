package com.mindcluster.safediary.carescheduling.domain.model.valueobjects;

import java.time.Duration;
import java.time.Instant;

/** Participants can enter the meeting from a few minutes before the start until the scheduled end. */
public record MeetingAccessWindow(Instant opensAt, Instant closesAt) {
    public static final Duration EARLY_ACCESS = Duration.ofMinutes(10);
    public MeetingAccessWindow {
        if (opensAt == null || closesAt == null || !closesAt.isAfter(opensAt))
            throw new IllegalArgumentException("Invalid meeting access window");
    }
    public static MeetingAccessWindow of(AppointmentSlot slot) {
        return new MeetingAccessWindow(slot.startsAt().minus(EARLY_ACCESS), slot.endsAt());
    }
    public boolean isOpenAt(Instant moment) { return !moment.isBefore(opensAt) && !moment.isAfter(closesAt); }
}
