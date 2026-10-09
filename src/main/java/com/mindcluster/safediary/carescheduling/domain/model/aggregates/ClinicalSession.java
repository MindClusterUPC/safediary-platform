package com.mindcluster.safediary.carescheduling.domain.model.aggregates;

import com.mindcluster.safediary.carescheduling.domain.model.events.CareSchedulingDomainEvent;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** Video call of a confirmed appointment. It stores operational data only, never clinical content. */
@Getter
public class ClinicalSession extends AbstractDomainAggregateRoot<ClinicalSession> {
    private final Long id;
    private final Long appointmentId;
    private final String providerRoomRef;
    private Instant startedAt;
    private Instant endedAt;
    private ClinicalSessionStatus status;

    public ClinicalSession(Long id, Long appointmentId, String providerRoomRef, Instant startedAt, Instant endedAt,
                           ClinicalSessionStatus status) {
        if (appointmentId == null || appointmentId <= 0) throw new IllegalArgumentException("Appointment is required");
        if (providerRoomRef == null || providerRoomRef.isBlank() || providerRoomRef.length() > 255)
            throw new IllegalArgumentException("Video room reference is required");
        this.id = id;
        this.appointmentId = appointmentId;
        this.providerRoomRef = providerRoomRef;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.status = Objects.requireNonNull(status, "Status is required");
    }

    public static ClinicalSession schedule(Appointment appointment, String providerRoomRef) {
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED)
            throw new IllegalStateException("Only confirmed appointments have a session");
        return new ClinicalSession(null, appointment.getId(), providerRoomRef, null, null, ClinicalSessionStatus.SCHEDULED);
    }

    public boolean isOpen() { return status == ClinicalSessionStatus.SCHEDULED || status == ClinicalSessionStatus.IN_PROGRESS; }

    /** The first authorized participant entering the room starts the session. */
    public void join(Instant now) {
        if (!isOpen()) throw new IllegalStateException("The session is already closed");
        if (status == ClinicalSessionStatus.SCHEDULED) {
            status = ClinicalSessionStatus.IN_PROGRESS;
            startedAt = now.truncatedTo(ChronoUnit.SECONDS);
            registerDomainEvent(new CareSchedulingDomainEvent("SessionStarted", null, appointmentId));
        }
    }

    public void close(SessionOutcome outcome, Instant now) {
        if (!isOpen()) throw new IllegalStateException("The session is already closed");
        if (outcome == null) throw new IllegalArgumentException("Outcome is required");
        if (outcome == SessionOutcome.COMPLETED && status != ClinicalSessionStatus.IN_PROGRESS)
            throw new IllegalStateException("A session must start before it can be completed");
        endedAt = now.truncatedTo(ChronoUnit.SECONDS);
        status = switch (outcome) {
            case COMPLETED -> ClinicalSessionStatus.COMPLETED;
            case NO_SHOW -> ClinicalSessionStatus.NO_SHOW;
            case NOT_HELD -> ClinicalSessionStatus.CANCELLED;
        };
        registerDomainEvent(new CareSchedulingDomainEvent(
                outcome == SessionOutcome.COMPLETED ? "SessionCompleted" : "SessionNotHeld", null, appointmentId));
    }

    public void cancel() {
        if (!isOpen()) return;
        status = ClinicalSessionStatus.CANCELLED;
    }

    /** Attended minutes reported to Clinician Directory; at least one minute. */
    public int attendedMinutes() {
        if (startedAt == null || endedAt == null) return 0;
        return (int) Math.max(1, ChronoUnit.MINUTES.between(startedAt, endedAt));
    }
}
