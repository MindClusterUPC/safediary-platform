package com.mindcluster.safediary.carescheduling.domain.model.events;

import java.time.Instant;

/**
 * Notification with identifiers only (ContactRequestSent, ScheduleProposed, ScheduleAccepted, SlotHeld, HoldExpired,
 * AppointmentConfirmed, AppointmentCancelled, SessionStarted, SessionCompleted, ...). Chat content never travels in events.
 */
public record CareSchedulingDomainEvent(String type, Long contactRequestId, Long appointmentId, Instant occurredAt) {
    public CareSchedulingDomainEvent(String type, Long contactRequestId, Long appointmentId) {
        this(type, contactRequestId, appointmentId, Instant.now());
    }
}
