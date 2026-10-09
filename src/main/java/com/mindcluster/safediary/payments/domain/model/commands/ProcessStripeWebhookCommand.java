package com.mindcluster.safediary.payments.domain.model.commands;

import java.util.Objects;

/**
 * Command carrying incoming Stripe webhook raw payload and signature header for processing.
 */
public record ProcessStripeWebhookCommand(
        String payloadJson,
        String stripeSignatureHeader
) {
    public ProcessStripeWebhookCommand {
        Objects.requireNonNull(payloadJson, "payloadJson cannot be null");
    }
}
