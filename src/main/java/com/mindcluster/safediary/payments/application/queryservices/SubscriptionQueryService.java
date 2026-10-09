package com.mindcluster.safediary.payments.application.queryservices;

import com.mindcluster.safediary.payments.domain.model.aggregates.Subscription;
import com.mindcluster.safediary.payments.domain.model.queries.GetSubscriptionByPatientIdQuery;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;

public interface SubscriptionQueryService {
    Result<Subscription, ApplicationError> handle(GetSubscriptionByPatientIdQuery query);
}
