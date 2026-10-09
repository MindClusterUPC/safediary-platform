package com.mindcluster.safediary.payments.application.outbound;

import com.mindcluster.safediary.payments.application.outbound.dto.StripeCheckoutSession;
import com.mindcluster.safediary.payments.application.outbound.dto.StripeWebhookEvent;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;

/**
 * Anti-Corruption Layer outbound interface decoupling the Payments application service
 * from external Stripe SDK operations and network protocols.
 */
public interface StripeGatewayClient {

    /**
     * Creates a Stripe Checkout Session for recurring plan subscriptions.
     * Applies Idempotency-Key on the Stripe request.
     */
    StripeCheckoutSession createSubscriptionCheckoutSession(
            Long patientAccountId,
            SubscriptionPlan plan,
            String successUrl,
            String cancelUrl,
            String idempotencyKey
    );

    /**
     * Cancels an active remote Stripe subscription.
     */
    void cancelSubscription(String stripeSubscriptionId);

    /**
     * Validates cryptographic Stripe signature and extracts normalized domain webhook event.
     * Throws an exception if signature verification fails or payload is malformed.
     */
    StripeWebhookEvent parseAndVerifyWebhook(String payload, String signatureHeader);
}
