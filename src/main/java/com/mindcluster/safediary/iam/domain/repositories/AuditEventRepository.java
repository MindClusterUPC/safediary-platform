package com.mindcluster.safediary.iam.domain.repositories;

import com.mindcluster.safediary.iam.domain.model.aggregates.AuditEvent;
import java.util.List;

/** Append-only: audit events are never updated or deleted. */
public interface AuditEventRepository {
    AuditEvent append(AuditEvent event);
    List<AuditEvent> findByTargetAccountId(Long targetAccountId);
}
