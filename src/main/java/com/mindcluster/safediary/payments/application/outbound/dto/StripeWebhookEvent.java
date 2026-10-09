package com.mindcluster.safediary.payments.application.outbound.dto;

import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;

import java.time.Instant;

/**
 * Normalized domain projection of a verified Stripe webhook notification.
 */
public record StripeWebhookEvent(
        String eventId,
        String eventType,
        Long patientAccountId,
        SubscriptionPlan plan,
        Instant periodEnd,
        String stripeSubscriptionId,
        String stripeCustomerId
) {}
