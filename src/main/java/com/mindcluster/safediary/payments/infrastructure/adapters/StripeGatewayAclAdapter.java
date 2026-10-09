package com.mindcluster.safediary.payments.infrastructure.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindcluster.safediary.payments.application.outbound.StripeGatewayClient;
import com.mindcluster.safediary.payments.application.outbound.dto.StripeCheckoutSession;
import com.mindcluster.safediary.payments.application.outbound.dto.StripeWebhookEvent;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;
import com.mindcluster.safediary.payments.infrastructure.configuration.StripeProperties;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class StripeGatewayAclAdapter implements StripeGatewayClient {

    private static final Logger log = LoggerFactory.getLogger(StripeGatewayAclAdapter.class);

    private final StripeProperties properties;
    private final ObjectMapper objectMapper;

    public StripeGatewayAclAdapter(StripeProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public StripeCheckoutSession createSubscriptionCheckoutSession(
            Long patientAccountId,
            SubscriptionPlan plan,
            String successUrl,
            String cancelUrl,
            String idempotencyKey) {

        String effectiveSuccessUrl = (successUrl != null && !successUrl.isBlank())
                ? successUrl
                : properties.getSuccessUrl();

        String effectiveCancelUrl = (cancelUrl != null && !cancelUrl.isBlank())
                ? cancelUrl
                : properties.getCancelUrl();

        String apiKey = properties.getApiKey();

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Stripe API key is not configured. Generating simulated checkout session for local development.");
            String simulatedSessionId = "cs_test_" + UUID.randomUUID().toString().replace("-", "");
            String simulatedUrl = "https://checkout.stripe.com/c/pay/" + simulatedSessionId;
            return new StripeCheckoutSession(simulatedSessionId, simulatedUrl);
        }

        try {
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(effectiveSuccessUrl)
                    .setCancelUrl(effectiveCancelUrl)
                    .setClientReferenceId(String.valueOf(patientAccountId))
                    .putMetadata("patientAccountId", String.valueOf(patientAccountId))
                    .putMetadata("plan", plan.name());

            String priceId = resolvePriceId(plan);
            if (priceId != null && !priceId.isBlank()) {
                paramsBuilder.addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId)
                                .setQuantity(1L)
                                .build()
                );
            } else {
                SessionCreateParams.LineItem.PriceData.Recurring.Interval interval =
                        (plan == SubscriptionPlan.ASTRUM)
                                ? SessionCreateParams.LineItem.PriceData.Recurring.Interval.YEAR
                                : SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH;

                long unitAmountCents = (plan == SubscriptionPlan.ASTRUM) ? 10000L : 1999L;

                SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("usd")
                        .setUnitAmount(unitAmountCents)
                        .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("SafeDiary " + plan.getDisplayName() + " Plan")
                                        .setDescription(plan == SubscriptionPlan.ASTRUM
                                                ? "SafeDiary Astrum annual subscription"
                                                : "SafeDiary Terra monthly subscription")
                                        .build()
                        )
                        .setRecurring(
                                SessionCreateParams.LineItem.PriceData.Recurring.builder()
                                        .setInterval(interval)
                                        .build()
                        )
                        .build();

                paramsBuilder.addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(priceData)
                                .setQuantity(1L)
                                .build()
                );
            }

            RequestOptions requestOptions = RequestOptions.builder()
                    .setApiKey(apiKey)
                    .setIdempotencyKey(idempotencyKey)
                    .build();

            Session session = Session.create(paramsBuilder.build(), requestOptions);
            return new StripeCheckoutSession(session.getId(), session.getUrl());
        } catch (Exception ex) {
            log.error("Failed to create Stripe checkout session", ex);
            throw new IllegalStateException("Error communicating with Stripe: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void cancelSubscription(String stripeSubscriptionId) {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Stripe API key not configured. Simulating subscription cancellation for {}", stripeSubscriptionId);
            return;
        }

        try {
            RequestOptions requestOptions = RequestOptions.builder()
                    .setApiKey(apiKey)
                    .build();

            com.stripe.model.Subscription subscription =
                    com.stripe.model.Subscription.retrieve(stripeSubscriptionId, requestOptions);
            subscription.cancel((com.stripe.param.SubscriptionCancelParams) null, requestOptions);
            log.info("Successfully cancelled Stripe subscription {}", stripeSubscriptionId);
        } catch (Exception ex) {
            log.error("Error cancelling remote Stripe subscription {}", stripeSubscriptionId, ex);
            throw new IllegalStateException("Could not cancel Stripe subscription: " + ex.getMessage(), ex);
        }
    }

    @Override
    public StripeWebhookEvent parseAndVerifyWebhook(String payload, String signatureHeader) {
        String webhookSecret = properties.getWebhookSecret();

        if (webhookSecret != null && !webhookSecret.isBlank()) {
            if (signatureHeader == null || signatureHeader.isBlank()) {
                throw new SecurityException("Missing Stripe-Signature header");
            }
            try {
                Webhook.constructEvent(payload, signatureHeader, webhookSecret);
            } catch (SignatureVerificationException ex) {
                throw new SecurityException("Stripe signature verification failed: " + ex.getMessage(), ex);
            }
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            String eventId = root.path("id").asText();
            String eventType = root.path("type").asText();

            if (eventId.isBlank()) {
                throw new IllegalArgumentException("Stripe webhook payload missing 'id'");
            }

            JsonNode dataObject = root.path("data").path("object");

            Long patientAccountId = null;
            if (dataObject.hasNonNull("client_reference_id")) {
                try {
                    patientAccountId = Long.parseLong(dataObject.path("client_reference_id").asText());
                } catch (NumberFormatException ignored) {}
            }

            if (patientAccountId == null && dataObject.has("metadata") && dataObject.path("metadata").hasNonNull("patientAccountId")) {
                try {
                    patientAccountId = Long.parseLong(dataObject.path("metadata").path("patientAccountId").asText());
                } catch (NumberFormatException ignored) {}
            }

            SubscriptionPlan plan = null;
            if (dataObject.has("metadata") && dataObject.path("metadata").hasNonNull("plan")) {
                try {
                    plan = SubscriptionPlan.fromString(dataObject.path("metadata").path("plan").asText());
                } catch (IllegalArgumentException ignored) {}
            }

            String stripeSubId = dataObject.path("subscription").asText(null);
            if (stripeSubId == null && "customer.subscription.deleted".equalsIgnoreCase(eventType)) {
                stripeSubId = dataObject.path("id").asText(null);
            }

            String stripeCustomerId = dataObject.path("customer").asText(null);

            Instant periodEnd = null;
            if (dataObject.hasNonNull("current_period_end")) {
                long epochSec = dataObject.path("current_period_end").asLong();
                if (epochSec > 0) {
                    periodEnd = Instant.ofEpochSecond(epochSec);
                }
            }

            return new StripeWebhookEvent(
                    eventId,
                    eventType,
                    patientAccountId,
                    plan,
                    periodEnd,
                    stripeSubId,
                    stripeCustomerId
            );
        } catch (SecurityException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid Stripe webhook payload structure: " + ex.getMessage(), ex);
        }
    }

    private String resolvePriceId(SubscriptionPlan plan) {
        if (plan == SubscriptionPlan.TERRA && properties.getSubscription() != null && properties.getSubscription().getTerra() != null) {
            return properties.getSubscription().getTerra().getPriceId();
        }
        if (plan == SubscriptionPlan.ASTRUM && properties.getSubscription() != null && properties.getSubscription().getAstrum() != null) {
            return properties.getSubscription().getAstrum().getPriceId();
        }
        return null;
    }
}
