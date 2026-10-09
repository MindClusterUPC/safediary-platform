package com.mindcluster.safediary.payments.domain.model.events;

import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;

import java.time.Instant;

/**
 * Domain event emitted when a patient subscription is activated or upgraded.
 */
public record SubscriptionActivatedEvent(
        Long patientAccountId,
        SubscriptionPlan plan,
        Instant activeUntil,
        String stripeSubscriptionId,
        Instant occurredAt
) {
    public SubscriptionActivatedEvent(Long patientAccountId, SubscriptionPlan plan, Instant activeUntil, String stripeSubscriptionId) {
        this(patientAccountId, plan, activeUntil, stripeSubscriptionId, Instant.now());
    }
}
