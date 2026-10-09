package com.mindcluster.safediary.cliniciandirectory.domain.model.events;

import java.time.Instant;

/** Integration notification containing identifiers only; private content stays in this context. */
public record DirectoryDomainEvent(String type, Long clinicianId, Long resourceId, Instant occurredAt) {
    public DirectoryDomainEvent(String type, Long clinicianId, Long resourceId) {
        this(type, clinicianId, resourceId, Instant.now());
    }
}
