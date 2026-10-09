package com.mindcluster.safediary.payments.domain.model.valueobjects;

/**
 * Status of a patient subscription.
 */
public enum SubscriptionStatus {
    ACTIVE,
    PENDING_PAYMENT,
    CANCELLED,
    EXPIRED;

    public boolean isActive() {
        return this == ACTIVE;
    }
}
