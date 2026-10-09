package com.mindcluster.safediary.carescheduling.domain.services;

import com.mindcluster.safediary.carescheduling.domain.model.aggregates.Appointment;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Builds the bookable grid of one agenda day from the weekly availability and the appointments holding slots. */
public final class BookableSlotCalculator {
    private BookableSlotCalculator() {}

    public static List<BookableSlot> calculate(LocalDate date, ZoneId zone, List<AvailabilityWindow> windows,
                                               int durationMinutes, int stepMinutes, List<Appointment> appointments,
                                               Instant now, boolean ownerView) {
        if (durationMinutes < 1 || stepMinutes < 1) throw new IllegalArgumentException("Duration and step must be positive");
        var slots = new ArrayList<BookableSlot>();
        windows.stream().filter(w -> w.dayOfWeek() == date.getDayOfWeek())
                .sorted(Comparator.comparing(AvailabilityWindow::startTime)).forEach(window -> {
            var start = date.atTime(window.startTime()).atZone(zone);
            var windowEnd = date.atTime(window.endTime()).atZone(zone);
            while (!start.plusMinutes(durationMinutes).isAfter(windowEnd)) {
                var startsAt = start.toInstant();
                var endsAt = start.plusMinutes(durationMinutes).toInstant();
                slots.add(new BookableSlot(startsAt, endsAt, availability(startsAt, endsAt, appointments, now, ownerView)));
                start = start.plusMinutes(stepMinutes);
            }
        });
        return slots;
    }

    private static SlotAvailability availability(Instant startsAt, Instant endsAt, List<Appointment> appointments,
                                                 Instant now, boolean ownerView) {
        var occupying = appointments.stream()
                .filter(a -> a.occupiesSlotAt(now) && a.getSlot().overlaps(startsAt, endsAt)).findFirst();
        if (occupying.isPresent()) {
            if (!ownerView) return SlotAvailability.UNAVAILABLE;
            return occupying.get().getStatus() == AppointmentStatus.CONFIRMED ? SlotAvailability.CONFIRMED : SlotAvailability.HELD;
        }
        return startsAt.isAfter(now.plus(Appointment.HOLD_DURATION)) ? SlotAvailability.FREE : SlotAvailability.UNAVAILABLE;
    }
}
