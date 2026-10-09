package com.mindcluster.safediary.cliniciandirectory.domain.model.entities;

import java.time.Instant;
public record CompletedSession(Long id, Long appointmentId, Long clinicianId, Long patientAccountId,
                               int durationMinutes, Instant completedAt) {
    public CompletedSession {
        if (appointmentId == null || appointmentId <= 0 || clinicianId == null || clinicianId <= 0
                || patientAccountId == null || patientAccountId <= 0 || durationMinutes <= 0
                || completedAt == null || completedAt.isAfter(Instant.now()))
            throw new IllegalArgumentException("A completed session requires valid identifiers, duration and completion time");
        completedAt = completedAt.truncatedTo(java.time.temporal.ChronoUnit.MICROS);
    }
}
