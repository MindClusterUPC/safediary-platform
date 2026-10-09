package com.mindcluster.safediary.carescheduling.domain.model.entities;

import java.time.Instant;

/** Audit of an authorized emotional summary read; stores only the consent reference, never the summary. */
public record SummaryAccessAudit(Long id, Long appointmentId, Long clinicianAccountId, String consentRef, Instant accessedAt) {
    public SummaryAccessAudit {
        if (appointmentId == null || clinicianAccountId == null || consentRef == null || consentRef.isBlank()
                || consentRef.length() > 128 || accessedAt == null)
            throw new IllegalArgumentException("Summary access audit requires appointment, clinician, consent and time");
    }
}
