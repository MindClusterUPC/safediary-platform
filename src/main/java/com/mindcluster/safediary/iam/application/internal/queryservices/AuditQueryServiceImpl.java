package com.mindcluster.safediary.iam.application.internal.queryservices;

import com.mindcluster.safediary.iam.application.queryservices.AuditQueryService;
import com.mindcluster.safediary.iam.domain.model.aggregates.AuditEvent;
import com.mindcluster.safediary.iam.domain.model.queries.GetAuditTrailByOwnerQuery;
import com.mindcluster.safediary.iam.domain.repositories.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class AuditQueryServiceImpl implements AuditQueryService {
    private final AuditEventRepository audits;

    /** Who accessed or changed what over the patient's information, newest first. */
    public List<AuditEvent> handle(GetAuditTrailByOwnerQuery q) {
        return audits.findByTargetAccountId(q.ownerAccountId()).stream()
                .sorted(Comparator.comparing(AuditEvent::occurredAt).reversed()).toList();
    }
}
