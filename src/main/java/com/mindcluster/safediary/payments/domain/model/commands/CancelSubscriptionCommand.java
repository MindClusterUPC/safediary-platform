package com.mindcluster.safediary.payments.domain.model.commands;

/**
 * Command to cancel a patient's recurring subscription.
 */
public record CancelSubscriptionCommand(
        Long patientAccountId
) {
    public CancelSubscriptionCommand {
        if (patientAccountId == null || patientAccountId <= 0) {
            throw new IllegalArgumentException("patientAccountId must be a positive non-null ID");
        }
    }
}
