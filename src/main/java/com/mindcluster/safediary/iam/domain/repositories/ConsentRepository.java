package com.mindcluster.safediary.iam.domain.repositories;

import com.mindcluster.safediary.iam.domain.model.aggregates.Consent;
import java.util.List;
import java.util.Optional;

public interface ConsentRepository {
    Optional<Consent> findById(Long id);
    List<Consent> findByOwnerAccountId(Long ownerAccountId);
    List<Consent> findByGranteeAccountId(Long granteeAccountId);
    /** Consents still marked ACTIVE where the account is owner or grantee. */
    List<Consent> findActiveInvolving(Long accountId);
    Consent save(Consent consent);
}
