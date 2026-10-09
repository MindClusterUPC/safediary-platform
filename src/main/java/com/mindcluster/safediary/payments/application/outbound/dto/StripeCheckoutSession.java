package com.mindcluster.safediary.payments.application.outbound.dto;

/**
 * Information returned when a Stripe Checkout session is created.
 */
public record StripeCheckoutSession(
        String sessionId,
        String checkoutUrl
) {}
