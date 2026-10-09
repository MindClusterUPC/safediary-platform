package com.mindcluster.safediary.payments.domain.model.events;

import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;

import java.time.Instant;

/**
 * Domain event emitted when a patient subscription is cancelled.
 */
public record SubscriptionCancelledEvent(
        Long patientAccountId,
        SubscriptionPlan plan,
        Instant occurredAt
) {
    public SubscriptionCancelledEvent(Long patientAccountId, SubscriptionPlan plan) {
        this(patientAccountId, plan, Instant.now());
    }
}
