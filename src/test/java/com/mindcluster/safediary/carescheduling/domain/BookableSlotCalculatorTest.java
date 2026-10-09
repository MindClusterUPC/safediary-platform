package com.mindcluster.safediary.carescheduling.domain;

import com.mindcluster.safediary.carescheduling.domain.model.aggregates.*;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import com.mindcluster.safediary.carescheduling.domain.services.BookableSlotCalculator;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class BookableSlotCalculatorTest {
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final LocalDate DAY = LocalDate.of(2026, 10, 9);
    private static final Instant NOW = Instant.parse("2026-10-08T21:36:00Z");

    private Appointment held(LocalTime start) {
        var startsAt = DAY.atTime(start).atZone(LIMA).toInstant();
        var request = new ContactRequest(1L, 20L, 3L, ContactRequestStatus.ACCEPTED, NOW);
        var appointment = Appointment.propose(request, new AppointmentSlot(startsAt, startsAt.plusSeconds(3000), LIMA.getId()),
                new AgreedAmount(new BigDecimal("120.00"), "PEN"), NOW);
        appointment.acceptAndHold(NOW);
        return appointment;
    }

    @Test void buildsHourlySlotsInsideAvailabilityAndHidesReservationsFromOthers() {
        var windows = List.of(new AvailabilityWindow(DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(12, 0)),
                new AvailabilityWindow(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0)));
        var appointments = List.of(held(LocalTime.of(10, 0)));

        var owner = BookableSlotCalculator.calculate(DAY, LIMA, windows, 50, 60, appointments, NOW, true);
        assertThat(owner).extracting(BookableSlot::availability)
                .containsExactly(SlotAvailability.FREE, SlotAvailability.HELD, SlotAvailability.FREE);
        assertThat(owner.getFirst().startsAt()).isEqualTo(Instant.parse("2026-10-09T14:00:00Z"));

        var patient = BookableSlotCalculator.calculate(DAY, LIMA, windows, 50, 60, appointments, NOW, false);
        assertThat(patient.get(1).availability()).isEqualTo(SlotAvailability.UNAVAILABLE);
    }

    @Test void slotsThatCannotBeHeldBeforeStartingAreUnavailable() {
        var windows = List.of(new AvailabilityWindow(DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(11, 0)));
        var almostThere = DAY.atTime(9, 30).atZone(LIMA).toInstant();
        var slots = BookableSlotCalculator.calculate(DAY, LIMA, windows, 50, 60, List.of(), almostThere, true);
        assertThat(slots).extracting(BookableSlot::availability).containsExactly(SlotAvailability.UNAVAILABLE, SlotAvailability.UNAVAILABLE);
    }
}
