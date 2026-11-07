package com.mindcluster.safediary.iam.application.queryservices;

import com.mindcluster.safediary.iam.domain.model.aggregates.Consent;
import com.mindcluster.safediary.iam.domain.model.queries.*;
import java.util.List;

public interface ConsentQueryService {
    List<Consent> handle(GetActiveConsentsByOwnerQuery query);
    List<Consent> handle(GetConsentScopeForSpecialistQuery query);
}
