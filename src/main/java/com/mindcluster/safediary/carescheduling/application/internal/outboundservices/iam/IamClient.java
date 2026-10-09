package com.mindcluster.safediary.carescheduling.application.internal.outboundservices.iam;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.CareActor;
import java.util.Optional;

/** Identity and consent from IAM (OHS + ACL). Consent is validated at read time and never cached. */
public interface IamClient {
    CareActor currentActor();

    /** Returns the reference of the active consent to share an emotional summary, or empty when there is none. */
    Optional<String> findActiveSummaryConsent(Long patientAccountId, Long clinicianAccountId);
}
