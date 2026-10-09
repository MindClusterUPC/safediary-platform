package com.mindcluster.safediary.carescheduling.domain;

import com.mindcluster.safediary.carescheduling.domain.model.aggregates.*;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import static org.assertj.core.api.Assertions.*;

class AppointmentTest {
    private static final Instant NOW = Instant.parse("2026-10-08T21:36:00Z");
    private static final Instant START = Instant.parse("2026-10-09T20:00:00Z");

    private ContactRequest request() {
        return new ContactRequest(7L, 20L, 3L, ContactRequestStatus.ACCEPTED, NOW);
    }
    private Appointment proposal() {
        var appointment = Appointment.propose(request(), new AppointmentSlot(START, START.plus(Duration.ofMinutes(50)), "America/Lima"),
                new AgreedAmount(new BigDecimal("120.00"), "pen"), NOW);
        return new Appointment(1L, appointment.getContactRequestId(), appointment.getPatientAccountId(), appointment.getClinicianId(),
                appointment.getSlot(), appointment.getAmount(), appointment.getStatus(), null, null);
    }

    @Test void proposalDoesNotOccupyTheSlotUntilAccepted() {
        var appointment = proposal();
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.REQUESTED);
        assertThat(appointment.getAmount().currency()).isEqualTo("PEN");
        assertThat(appointment.occupiesSlotAt(NOW)).isFalse();
        appointment.acceptAndHold(NOW);
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.HELD);
        assertThat(appointment.getHold().getExpiresAt()).isEqualTo(NOW.plus(Duration.ofHours(1)));
        assertThat(appointment.occupiesSlotAt(NOW.plusSeconds(3599))).isTrue();
        assertThat(appointment.occupiesSlotAt(NOW.plusSeconds(3600))).isFalse();
    }

    @Test void proposalMustLeaveTimeForThePaymentHold() {
        var soon = NOW.plus(Duration.ofMinutes(30));
        assertThatThrownBy(() -> Appointment.propose(request(), new AppointmentSlot(soon, soon.plusSeconds(3000), "America/Lima"),
                new AgreedAmount(BigDecimal.TEN, "PEN"), NOW)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test void onlyAMatchingPaymentForAnActiveHoldConfirms() {
        var appointment = proposal();
        appointment.acceptAndHold(NOW);
        assertThatThrownBy(() -> appointment.confirmPayment("PAY-1", new BigDecimal("100.00"), "PEN", NOW.plusSeconds(60)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> appointment.confirmPayment("PAY-1", new BigDecimal("120.00"), "PEN", NOW.plusSeconds(3600)))
                .isInstanceOf(IllegalStateException.class);
        appointment.confirmPayment("PAY-1", new BigDecimal("120"), "pen", NOW.plusSeconds(60));
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(appointment.getHold().getStatus()).isEqualTo(SlotHoldStatus.CONFIRMED);
        assertThat(appointment.getPaymentReference()).isEqualTo("PAY-1");
    }

    @Test void expiredHoldReleasesTheSlotAndCannotBeCancelledAgain() {
        var appointment = proposal();
        appointment.acceptAndHold(NOW);
        assertThatThrownBy(() -> appointment.expireHold(NOW.plusSeconds(10))).isInstanceOf(IllegalStateException.class);
        appointment.expireHold(NOW.plusSeconds(3600));
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.EXPIRED);
        assertThat(appointment.getHold().getStatus()).isEqualTo(SlotHoldStatus.EXPIRED);
        assertThatThrownBy(appointment::cancel).isInstanceOf(IllegalStateException.class);
    }

    @Test void cancellingAHoldReleasesIt() {
        var appointment = proposal();
        appointment.acceptAndHold(NOW);
        appointment.cancel();
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        assertThat(appointment.getHold().getStatus()).isEqualTo(SlotHoldStatus.RELEASED);
    }

    @Test void participantsAreThePatientAndTheClinician() {
        var appointment = proposal();
        assertThat(appointment.hasParticipant(new CareActor(20L, CareRole.PATIENT, null))).isTrue();
        assertThat(appointment.hasParticipant(new CareActor(21L, CareRole.PATIENT, null))).isFalse();
        assertThat(appointment.hasParticipant(new CareActor(99L, CareRole.PSYCHOLOGIST, 3L))).isTrue();
        assertThat(appointment.hasParticipant(new CareActor(99L, CareRole.PSYCHOLOGIST, 4L))).isFalse();
    }

    @Test void sessionMustStartBeforeBeingCompleted() {
        var appointment = proposal();
        appointment.acceptAndHold(NOW);
        appointment.confirmPayment("PAY-1", new BigDecimal("120.00"), "PEN", NOW);
        var session = ClinicalSession.schedule(appointment, "room");
        assertThatThrownBy(() -> session.close(SessionOutcome.COMPLETED, START)).isInstanceOf(IllegalStateException.class);
        session.join(START);
        session.close(SessionOutcome.COMPLETED, START.plus(Duration.ofMinutes(48)));
        assertThat(session.getStatus()).isEqualTo(ClinicalSessionStatus.COMPLETED);
        assertThat(session.attendedMinutes()).isEqualTo(48);
    }
}
