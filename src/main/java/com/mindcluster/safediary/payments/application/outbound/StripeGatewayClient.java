package com.mindcluster.safediary.payments.application.outbound;

import com.mindcluster.safediary.payments.application.outbound.dto.StripeCheckoutSession;
import com.mindcluster.safediary.payments.application.outbound.dto.StripeWebhookEvent;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;

import java.math.BigDecimal;

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
     * Creates a Stripe Express connected account for a psychologist.
     *
     * @param psychologistAccountId SafeDiary account ID
     * @param email                Psychologist email
     * @return Stripe account ID (acct_...)
     */
    String createConnectedAccount(Long psychologistAccountId, String email);

    /**
     * Creates a single-use onboarding AccountLink URL for a connected account.
     *
     * @param stripeAccountId Stripe connected account ID (acct_...)
     * @param refreshUrl      URL if link expires or fails
     * @param returnUrl       URL on successful onboarding completion
     * @return Onboarding redirect URL
     */
    String createAccountLink(String stripeAccountId, String refreshUrl, String returnUrl);

    /**
     * Creates a direct destination charge / split payment Stripe Checkout Session for a consultation.
     *
     * @param patientAccountId           Patient account ID
     * @param psychologistAccountId       Psychologist account ID
     * @param destinationStripeAccountId  Psychologist's Stripe connected account ID (acct_...)
     * @param totalAmount                Total amount paid by the patient
     * @param applicationFeeAmount       Platform commission retained by SafeDiary
     * @param currency                   ISO currency code (e.g., "USD")
     * @param appointmentId              Care appointment ID
     * @param successUrl                 Return URL on success
     * @param cancelUrl                  Return URL on cancellation
     * @param idempotencyKey             Idempotency key
     * @return Stripe checkout session details
     */
    StripeCheckoutSession createConsultationCheckoutSession(
            Long patientAccountId,
            Long psychologistAccountId,
            String destinationStripeAccountId,
            BigDecimal totalAmount,
            BigDecimal applicationFeeAmount,
            String currency,
            Long appointmentId,
            String successUrl,
            String cancelUrl,
            String idempotencyKey
    );

    /**
     * Validates cryptographic Stripe signature and extracts normalized domain webhook event.
     * Supports both platform and Connect webhook signatures.
     * Throws an exception if signature verification fails or payload is malformed.
     */
    StripeWebhookEvent parseAndVerifyWebhook(String payload, String signatureHeader);
}
