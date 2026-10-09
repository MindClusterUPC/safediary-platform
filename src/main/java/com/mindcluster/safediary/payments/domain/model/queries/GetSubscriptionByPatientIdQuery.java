package com.mindcluster.safediary.payments.domain.model.queries;

/**
 * Query to retrieve a patient's current subscription details.
 */
public record GetSubscriptionByPatientIdQuery(
        Long patientAccountId
) {
    public GetSubscriptionByPatientIdQuery {
        if (patientAccountId == null || patientAccountId <= 0) {
            throw new IllegalArgumentException("patientAccountId must be a positive non-null ID");
        }
    }
}
