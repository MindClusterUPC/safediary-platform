package com.mindcluster.safediary.iam.application.internal.queryservices;

import com.mindcluster.safediary.iam.application.queryservices.ConsentQueryService;
import com.mindcluster.safediary.iam.domain.model.aggregates.Consent;
import com.mindcluster.safediary.iam.domain.model.queries.*;
import com.mindcluster.safediary.iam.domain.repositories.ConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/** Only consents in force are listed: revoked or expired ones disappear immediately (US-023, US-046). */
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class ConsentQueryServiceImpl implements ConsentQueryService {
    private final ConsentRepository consents;

    private List<Consent> inForce(List<Consent> values) {
        var now = Instant.now();
        return values.stream().filter(c -> c.isActiveAt(now)).sorted(Comparator.comparing(Consent::getGrantedAt).reversed()).toList();
    }

    public List<Consent> handle(GetActiveConsentsByOwnerQuery q) { return inForce(consents.findByOwnerAccountId(q.ownerAccountId())); }

    public List<Consent> handle(GetConsentScopeForSpecialistQuery q) { return inForce(consents.findByGranteeAccountId(q.granteeAccountId())); }
}
