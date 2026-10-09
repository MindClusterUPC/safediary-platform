package com.mindcluster.safediary.payments.domain.model.commands;

import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;

/**
 * Command to initialize a subscription checkout for a patient.
 */
public record CreateSubscriptionCheckoutCommand(
        Long patientAccountId,
        SubscriptionPlan plan,
        String successUrl,
        String cancelUrl,
        String idempotencyKey
) {
    public CreateSubscriptionCheckoutCommand {
        if (patientAccountId == null || patientAccountId <= 0) {
            throw new IllegalArgumentException("patientAccountId must be a positive non-null ID");
        }
        if (plan == null) {
            plan = SubscriptionPlan.FREE;
        }
    }
}
