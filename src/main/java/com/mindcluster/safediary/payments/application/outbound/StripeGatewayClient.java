package com.mindcluster.safediary.payments.application.outbound;

import com.mindcluster.safediary.payments.application.outbound.dto.StripeCheckoutSession;
import com.mindcluster.safediary.payments.application.outbound.dto.StripePaymentMethodDto;
import com.mindcluster.safediary.payments.application.outbound.dto.StripeWebhookEvent;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;

import java.math.BigDecimal;
import java.util.List;

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
     * Retrieves saved card payment methods for a customer in Stripe.
     */
    List<StripePaymentMethodDto> listCustomerPaymentMethods(String stripeCustomerId);

    /**
     * Validates cryptographic Stripe signature and extracts normalized domain webhook event.
     * Supports both platform and Connect webhook signatures.
     * Throws an exception if signature verification fails or payload is malformed.
     */
    StripeWebhookEvent parseAndVerifyWebhook(String payload, String signatureHeader);
}
