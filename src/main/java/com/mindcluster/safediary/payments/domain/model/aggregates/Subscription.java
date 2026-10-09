package com.mindcluster.safediary.payments.domain.model.aggregates;

import com.mindcluster.safediary.payments.domain.model.events.SubscriptionActivatedEvent;
import com.mindcluster.safediary.payments.domain.model.events.SubscriptionCancelledEvent;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionStatus;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Subscription Aggregate Root.
 * Manages patient membership tier (FREE, TERRA, ASTRUM), billing cycle boundaries,
 * and Stripe subscription state.
 */
@Getter
public class Subscription extends AbstractDomainAggregateRoot<Subscription> {

    private Long id;
    private final Long patientAccountId;
    private SubscriptionPlan plan;
    private SubscriptionStatus status;
    private Instant activeUntil;
    private String stripeSubscriptionId;
    private String stripeCustomerId;

    public Subscription(Long patientAccountId,
                        SubscriptionPlan plan,
                        SubscriptionStatus status,
                        Instant activeUntil,
                        String stripeSubscriptionId,
                        String stripeCustomerId) {
        if (patientAccountId == null || patientAccountId <= 0) {
            throw new IllegalArgumentException("patientAccountId must be a positive non-null ID");
        }
        this.patientAccountId = patientAccountId;
        this.plan = Objects.requireNonNullElse(plan, SubscriptionPlan.FREE);
        this.status = Objects.requireNonNullElse(status, SubscriptionStatus.ACTIVE);
        this.activeUntil = activeUntil;
        this.stripeSubscriptionId = stripeSubscriptionId;
        this.stripeCustomerId = stripeCustomerId;
    }

    public Subscription(Long id,
                        Long patientAccountId,
                        SubscriptionPlan plan,
                        SubscriptionStatus status,
                        Instant activeUntil,
                        String stripeSubscriptionId,
                        String stripeCustomerId) {
        this(patientAccountId, plan, status, activeUntil, stripeSubscriptionId, stripeCustomerId);
        this.id = id;
    }

    /**
     * Factory method creating a default FREE tier subscription for a patient.
     */
    public static Subscription createFree(Long patientAccountId) {
        return new Subscription(
                patientAccountId,
                SubscriptionPlan.FREE,
                SubscriptionStatus.ACTIVE,
                null,
                null,
                null
        );
    }

    /**
     * Resets or assigns the FREE tier to this subscription.
     */
    public void assignFreePlan() {
        this.plan = SubscriptionPlan.FREE;
        this.status = SubscriptionStatus.ACTIVE;
        this.activeUntil = null;
        this.stripeSubscriptionId = null;
    }

    /**
     * Transitions subscription to PENDING_PAYMENT during checkout initiation.
     */
    public void markPendingCheckout(SubscriptionPlan targetPlan, String stripeCustomerId) {
        if (targetPlan != null && targetPlan != SubscriptionPlan.FREE) {
            this.plan = targetPlan;
            this.status = SubscriptionStatus.PENDING_PAYMENT;
            if (stripeCustomerId != null && !stripeCustomerId.isBlank()) {
                this.stripeCustomerId = stripeCustomerId;
            }
        }
    }

    /**
     * Activates a paid or upgraded subscription upon confirmed checkout/webhook.
     */
    public void activateSubscription(SubscriptionPlan newPlan,
                                     Instant activeUntil,
                                     String stripeSubscriptionId,
                                     String stripeCustomerId) {
        this.plan = Objects.requireNonNull(newPlan, "newPlan is required");
        this.status = SubscriptionStatus.ACTIVE;
        this.activeUntil = activeUntil;
        if (stripeSubscriptionId != null && !stripeSubscriptionId.isBlank()) {
            this.stripeSubscriptionId = stripeSubscriptionId;
        }
        if (stripeCustomerId != null && !stripeCustomerId.isBlank()) {
            this.stripeCustomerId = stripeCustomerId;
        }

        registerDomainEvent(new SubscriptionActivatedEvent(
                this.patientAccountId,
                this.plan,
                this.activeUntil,
                this.stripeSubscriptionId
        ));
    }

    /**
     * Cancels the subscription. The patient retains access until activeUntil (if set).
     */
    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
        registerDomainEvent(new SubscriptionCancelledEvent(
                this.patientAccountId,
                this.plan
        ));
    }

    /**
     * Marks the subscription as expired.
     */
    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
    }

    /**
     * Returns true if the subscription is currently active and within its valid time window.
     */
    public boolean isActive() {
        if (this.status != SubscriptionStatus.ACTIVE) {
            return false;
        }
        return this.activeUntil == null || this.activeUntil.isAfter(Instant.now());
    }
}
