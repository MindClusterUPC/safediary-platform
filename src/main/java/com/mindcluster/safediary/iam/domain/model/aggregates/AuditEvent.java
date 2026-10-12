package com.mindcluster.safediary.iam.domain.model.aggregates;

import com.mindcluster.safediary.iam.domain.model.valueobjects.AuditAction;
import java.time.Instant;

/**
 * Append-only trace of an access, consent change, vault unlock or denied attempt. It records who, over whom,
 * under which consent and on which resource, never the emotional content itself.
 */
public record AuditEvent(Long id, Long actorAccountId, Long targetAccountId, Long consentId, AuditAction action,
                         String sourceContext, String resourceType, Long resourceId, Instant occurredAt) {
    public AuditEvent {
        if (action == null || sourceContext == null || sourceContext.isBlank() || occurredAt == null)
            throw new IllegalArgumentException("An audit event requires an action, a source context and a date");
    }

    public static AuditEvent record(Long actor, Long target, Long consentId, AuditAction action,
                                    String sourceContext, String resourceType, Long resourceId) {
        return new AuditEvent(null, actor, target, consentId, action, sourceContext, resourceType, resourceId, Instant.now());
    }
}
