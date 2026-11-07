package com.mindcluster.safediary.iam.application.queryservices;

import com.mindcluster.safediary.iam.domain.model.aggregates.AuditEvent;
import com.mindcluster.safediary.iam.domain.model.queries.GetAuditTrailByOwnerQuery;
import java.util.List;

public interface AuditQueryService {
    List<AuditEvent> handle(GetAuditTrailByOwnerQuery query);
}
