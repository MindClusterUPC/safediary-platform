package com.mindcluster.safediary.payments.application.outbound.dto;

import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;

import java.time.Instant;

/**
 * Normalized domain projection of a verified Stripe webhook notification.
 * Supports both platform events (subscriptions, checkout) and Stripe Connect events
 * (connected account onboarding updates, payouts).
 */
public record StripeWebhookEvent(
        String eventId,
        String eventType,
        Long patientAccountId,
        SubscriptionPlan plan,
        Instant periodEnd,
        String stripeSubscriptionId,
        String stripeCustomerId,
        String connectedAccountId,
        String paymentType,
        Long psychologistAccountId,
        Long appointmentId,
        String paymentIntentId,
        Boolean chargesEnabled,
        Boolean payoutsEnabled,
        Boolean detailsSubmitted,
        String payoutId,
        Long payoutAmountCents,
        String payoutCurrency,
        String payoutStatus,
        String subscriptionStatus
) {
    public StripeWebhookEvent(
            String eventId,
            String eventType,
            Long patientAccountId,
            SubscriptionPlan plan,
            Instant periodEnd,
            String stripeSubscriptionId,
            String stripeCustomerId
    ) {
        this(eventId, eventType, patientAccountId, plan, periodEnd, stripeSubscriptionId, stripeCustomerId,
                null, "SUBSCRIPTION", null, null, null, null, null, null, null, null, null, null, null);
    }
}
