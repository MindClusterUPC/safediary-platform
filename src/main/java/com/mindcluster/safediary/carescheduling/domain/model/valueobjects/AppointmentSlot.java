package com.mindcluster.safediary.carescheduling.domain.model.valueobjects;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public record AppointmentSlot(Instant startsAt, Instant endsAt, String timezone) {
    public AppointmentSlot {
        if (startsAt == null || endsAt == null || !endsAt.isAfter(startsAt))
            throw new IllegalArgumentException("The slot end must be after its start");
        if (timezone == null || timezone.isBlank()) throw new IllegalArgumentException("Timezone is required");
        ZoneId.of(timezone);
        startsAt = startsAt.truncatedTo(ChronoUnit.SECONDS);
        endsAt = endsAt.truncatedTo(ChronoUnit.SECONDS);
    }
    public boolean overlaps(Instant otherStart, Instant otherEnd) {
        return startsAt.isBefore(otherEnd) && endsAt.isAfter(otherStart);
    }
    public int durationMinutes() { return (int) ChronoUnit.MINUTES.between(startsAt, endsAt); }
}
