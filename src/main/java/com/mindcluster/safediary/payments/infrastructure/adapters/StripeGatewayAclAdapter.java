package com.mindcluster.safediary.payments.infrastructure.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindcluster.safediary.payments.application.outbound.StripeGatewayClient;
import com.mindcluster.safediary.payments.application.outbound.dto.StripeCheckoutSession;
import com.mindcluster.safediary.payments.application.outbound.dto.StripeWebhookEvent;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;
import com.mindcluster.safediary.payments.infrastructure.configuration.StripeProperties;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Payout;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Component
public class StripeGatewayAclAdapter implements StripeGatewayClient {

    private static final Logger log = LoggerFactory.getLogger(StripeGatewayAclAdapter.class);

    private final StripeProperties properties;
    private final ObjectMapper objectMapper;

    public StripeGatewayAclAdapter(StripeProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
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
            // Subscription data metadata is duplicated so created Subscription aggregate retains user context
            SessionCreateParams.SubscriptionData subscriptionData = SessionCreateParams.SubscriptionData.builder()
                    .putMetadata("userId", String.valueOf(patientAccountId))
                    .putMetadata("patientAccountId", String.valueOf(patientAccountId))
                    .putMetadata("plan", plan.name())
                    .build();

            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(effectiveSuccessUrl)
                    .setCancelUrl(effectiveCancelUrl)
                    .setClientReferenceId(String.valueOf(patientAccountId))
                    .putMetadata("userId", String.valueOf(patientAccountId))
                    .putMetadata("patientAccountId", String.valueOf(patientAccountId))
                    .putMetadata("plan", plan.name())
                    .setSubscriptionData(subscriptionData);

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
    public String createConnectedAccount(Long psychologistAccountId, String email) {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Stripe API key not configured. Simulating Express connected account for psychologist {}", psychologistAccountId);
            return "acct_simulated_" + psychologistAccountId;
        }

        try {
            RequestOptions requestOptions = RequestOptions.builder()
                    .setApiKey(apiKey)
                    .build();

            AccountCreateParams.Builder paramsBuilder = AccountCreateParams.builder()
                    .setType(AccountCreateParams.Type.EXPRESS)
                    .setCapabilities(
                            AccountCreateParams.Capabilities.builder()
                                    .setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder().setRequested(true).build())
                                    .setTransfers(AccountCreateParams.Capabilities.Transfers.builder().setRequested(true).build())
                                    .build()
                    )
                    .putMetadata("psychologistAccountId", String.valueOf(psychologistAccountId))
                    .putMetadata("userId", String.valueOf(psychologistAccountId));

            if (email != null && !email.isBlank()) {
                paramsBuilder.setEmail(email.trim());
            }

            Account account = Account.create(paramsBuilder.build(), requestOptions);
            log.info("Created Stripe Express connected account {} for psychologist {}", account.getId(), psychologistAccountId);
            return account.getId();
        } catch (Exception ex) {
            log.error("Failed to create Stripe connected account for psychologist {}", psychologistAccountId, ex);
            throw new IllegalStateException("Could not create Stripe connected account: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String createAccountLink(String stripeAccountId, String refreshUrl, String returnUrl) {
        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Stripe API key not configured. Simulating AccountLink for {}", stripeAccountId);
            return "https://connect.stripe.com/express/onboarding/simulated_" + stripeAccountId;
        }

        try {
            RequestOptions requestOptions = RequestOptions.builder()
                    .setApiKey(apiKey)
                    .build();

            String effectiveRefresh = (refreshUrl != null && !refreshUrl.isBlank())
                    ? refreshUrl
                    : properties.getConnect().getRefreshUrl();

            String effectiveReturn = (returnUrl != null && !returnUrl.isBlank())
                    ? returnUrl
                    : properties.getConnect().getReturnUrl();

            AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                    .setAccount(stripeAccountId)
                    .setRefreshUrl(effectiveRefresh)
                    .setReturnUrl(effectiveReturn)
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build();

            AccountLink link = AccountLink.create(params, requestOptions);
            return link.getUrl();
        } catch (Exception ex) {
            log.error("Failed to create Stripe AccountLink for account {}", stripeAccountId, ex);
            throw new IllegalStateException("Could not create Stripe onboarding link: " + ex.getMessage(), ex);
        }
    }

    @Override
    public StripeCheckoutSession createConsultationCheckoutSession(
            Long patientAccountId,
            Long psychologistAccountId,
            String destinationStripeAccountId,
            BigDecimal totalAmount,
            BigDecimal applicationFeeAmount,
            String currency,
            Long appointmentId,
            String successUrl,
            String cancelUrl,
            String idempotencyKey) {

        String apiKey = properties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Stripe API key not configured. Simulating consultation checkout session for appointment {}", appointmentId);
            String simulatedSessionId = "cs_test_consultation_" + UUID.randomUUID().toString().replace("-", "");
            String simulatedUrl = "https://checkout.stripe.com/pay/" + simulatedSessionId;
            return new StripeCheckoutSession(simulatedSessionId, simulatedUrl);
        }

        try {
            String effectiveSuccessUrl = (successUrl != null && !successUrl.isBlank())
                    ? successUrl
                    : properties.getSuccessUrl();

            String effectiveCancelUrl = (cancelUrl != null && !cancelUrl.isBlank())
                    ? cancelUrl
                    : properties.getCancelUrl();

            String effectiveCurrency = (currency != null && !currency.isBlank())
                    ? currency.toLowerCase(Locale.ROOT)
                    : "usd";

            long totalAmountCents = totalAmount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
            long feeAmountCents = applicationFeeAmount != null
                    ? applicationFeeAmount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue()
                    : 0L;

            SessionCreateParams.PaymentIntentData.TransferData transferData =
                    SessionCreateParams.PaymentIntentData.TransferData.builder()
                            .setDestination(destinationStripeAccountId)
                            .build();

            SessionCreateParams.PaymentIntentData.Builder paymentIntentDataBuilder =
                    SessionCreateParams.PaymentIntentData.builder()
                            .setTransferData(transferData)
                            .putMetadata("patientAccountId", String.valueOf(patientAccountId))
                            .putMetadata("psychologistAccountId", String.valueOf(psychologistAccountId))
                            .putMetadata("type", "CONSULTATION");

            if (feeAmountCents > 0) {
                paymentIntentDataBuilder.setApplicationFeeAmount(feeAmountCents);
            }
            if (appointmentId != null) {
                paymentIntentDataBuilder.putMetadata("appointmentId", String.valueOf(appointmentId));
            }

            SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency(effectiveCurrency)
                    .setUnitAmount(totalAmountCents)
                    .setProductData(
                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Psychological Consultation")
                                    .setDescription("SafeDiary Video Care Session (Appointment #" + appointmentId + ")")
                                    .build()
                    )
                    .build();

            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(effectiveSuccessUrl)
                    .setCancelUrl(effectiveCancelUrl)
                    .setClientReferenceId(String.valueOf(patientAccountId))
                    .setPaymentIntentData(paymentIntentDataBuilder.build())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPriceData(priceData)
                                    .setQuantity(1L)
                                    .build()
                    )
                    .putMetadata("patientAccountId", String.valueOf(patientAccountId))
                    .putMetadata("psychologistAccountId", String.valueOf(psychologistAccountId))
                    .putMetadata("type", "CONSULTATION");

            if (appointmentId != null) {
                paramsBuilder.putMetadata("appointmentId", String.valueOf(appointmentId));
            }

            RequestOptions requestOptions = RequestOptions.builder()
                    .setApiKey(apiKey)
                    .setIdempotencyKey(idempotencyKey)
                    .build();

            Session session = Session.create(paramsBuilder.build(), requestOptions);
            return new StripeCheckoutSession(session.getId(), session.getUrl());
        } catch (Exception ex) {
            log.error("Failed to create Stripe consultation checkout session", ex);
            throw new IllegalStateException("Error communicating with Stripe for consultation: " + ex.getMessage(), ex);
        }
    }

    @Override
    public StripeWebhookEvent parseAndVerifyWebhook(String payload, String signatureHeader) {
        String webhookSecret = properties.getWebhookSecret();
        String connectWebhookSecret = properties.getConnectWebhookSecret();

        Event event = null;

        // Cryptographic signature verification with fallback to connect webhook secret
        if ((webhookSecret != null && !webhookSecret.isBlank()) || (connectWebhookSecret != null && !connectWebhookSecret.isBlank())) {
            if (signatureHeader == null || signatureHeader.isBlank()) {
                throw new SecurityException("Missing Stripe-Signature header");
            }

            SignatureVerificationException platformException = null;
            boolean verified = false;

            if (webhookSecret != null && !webhookSecret.isBlank()) {
                try {
                    event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
                    verified = true;
                } catch (SignatureVerificationException ex) {
                    platformException = ex;
                }
            }

            if (!verified && connectWebhookSecret != null && !connectWebhookSecret.isBlank()) {
                try {
                    event = Webhook.constructEvent(payload, signatureHeader, connectWebhookSecret);
                    verified = true;
                    log.info("Stripe webhook successfully verified using Connect webhook secret.");
                } catch (SignatureVerificationException ex) {
                    log.warn("Stripe Connect signature verification failed: {}", ex.getMessage());
                }
            }

            if (!verified) {
                throw new SecurityException("Stripe signature verification failed: "
                        + (platformException != null ? platformException.getMessage() : "Invalid signature"), platformException);
            }
        }

        try {
            // Deserialization with fallback to deserializeUnsafe()
            StripeObject stripeObject = null;
            if (event != null && event.getDataObjectDeserializer() != null) {
                EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
                if (deserializer.getObject().isPresent()) {
                    stripeObject = deserializer.getObject().get();
                } else {
                    try {
                        stripeObject = deserializer.deserializeUnsafe();
                        log.info("Stripe event {} deserialized safely via deserializeUnsafe() fallback.", event.getId());
                    } catch (Exception ex) {
                        log.warn("deserializeUnsafe() fallback failed for event {}: {}", event.getId(), ex.getMessage());
                    }
                }
            }

            // Fallback / complement with raw JsonNode parsing
            JsonNode root = objectMapper.readTree(payload);
            String eventId = event != null ? event.getId() : root.path("id").asText();
            String eventType = event != null ? event.getType() : root.path("type").asText();
            String connectedAccount = event != null ? event.getAccount() : root.path("account").asText(null);

            if (eventId == null || eventId.isBlank()) {
                throw new IllegalArgumentException("Stripe webhook payload missing 'id'");
            }

            JsonNode dataObjectNode = root.path("data").path("object");

            // Extract metadata from typed object or JsonNode
            Long patientAccountId = null;
            Long psychologistAccountId = null;
            Long appointmentId = null;
            SubscriptionPlan plan = null;
            String paymentType = "SUBSCRIPTION";
            String stripeSubId = null;
            String stripeCustomerId = null;
            Instant periodEnd = null;
            String paymentIntentId = null;
            Boolean chargesEnabled = null;
            Boolean payoutsEnabled = null;
            Boolean detailsSubmitted = null;
            String payoutId = null;
            Long payoutAmountCents = null;
            String payoutCurrency = null;
            String payoutStatus = null;
            String subscriptionStatus = null;

            // 1. If stripeObject is checkout Session
            if (stripeObject instanceof Session session) {
                Map<String, String> metadata = session.getMetadata();
                if (metadata != null) {
                    if (metadata.containsKey("patientAccountId")) {
                        try { patientAccountId = Long.parseLong(metadata.get("patientAccountId")); } catch (Exception ignored) {}
                    }
                    if (patientAccountId == null && metadata.containsKey("userId")) {
                        try { patientAccountId = Long.parseLong(metadata.get("userId")); } catch (Exception ignored) {}
                    }
                    if (metadata.containsKey("psychologistAccountId")) {
                        try { psychologistAccountId = Long.parseLong(metadata.get("psychologistAccountId")); } catch (Exception ignored) {}
                    }
                    if (metadata.containsKey("appointmentId")) {
                        try { appointmentId = Long.parseLong(metadata.get("appointmentId")); } catch (Exception ignored) {}
                    }
                    if (metadata.containsKey("plan")) {
                        try { plan = SubscriptionPlan.fromString(metadata.get("plan")); } catch (Exception ignored) {}
                    }
                    if (metadata.containsKey("type")) {
                        paymentType = metadata.get("type");
                    }
                }
                if (patientAccountId == null && session.getClientReferenceId() != null) {
                    try { patientAccountId = Long.parseLong(session.getClientReferenceId()); } catch (Exception ignored) {}
                }
                stripeSubId = session.getSubscription();
                stripeCustomerId = session.getCustomer();
                paymentIntentId = session.getPaymentIntent();
            }
            // 2. If stripeObject is com.stripe.model.Subscription
            else if (stripeObject instanceof com.stripe.model.Subscription sub) {
                Map<String, String> metadata = sub.getMetadata();
                if (metadata != null) {
                    if (metadata.containsKey("patientAccountId")) {
                        try { patientAccountId = Long.parseLong(metadata.get("patientAccountId")); } catch (Exception ignored) {}
                    }
                    if (patientAccountId == null && metadata.containsKey("userId")) {
                        try { patientAccountId = Long.parseLong(metadata.get("userId")); } catch (Exception ignored) {}
                    }
                    if (metadata.containsKey("plan")) {
                        try { plan = SubscriptionPlan.fromString(metadata.get("plan")); } catch (Exception ignored) {}
                    }
                }
                stripeSubId = sub.getId();
                stripeCustomerId = sub.getCustomer();
                subscriptionStatus = sub.getStatus();
                if (sub.getCurrentPeriodEnd() != null && sub.getCurrentPeriodEnd() > 0) {
                    periodEnd = Instant.ofEpochSecond(sub.getCurrentPeriodEnd());
                }
            }
            // 3. If stripeObject is Connect Account
            else if (stripeObject instanceof Account account) {
                connectedAccount = account.getId();
                chargesEnabled = account.getChargesEnabled();
                payoutsEnabled = account.getPayoutsEnabled();
                detailsSubmitted = account.getDetailsSubmitted();
                Map<String, String> metadata = account.getMetadata();
                if (metadata != null && metadata.containsKey("psychologistAccountId")) {
                    try { psychologistAccountId = Long.parseLong(metadata.get("psychologistAccountId")); } catch (Exception ignored) {}
                }
            }
            // 4. If stripeObject is Payout
            else if (stripeObject instanceof Payout payout) {
                payoutId = payout.getId();
                payoutAmountCents = payout.getAmount();
                payoutCurrency = payout.getCurrency();
                payoutStatus = payout.getStatus();
            }

            // Fallback to JSON node for any values not resolved by typed object
            if (patientAccountId == null) {
                if (dataObjectNode.hasNonNull("client_reference_id")) {
                    try { patientAccountId = Long.parseLong(dataObjectNode.path("client_reference_id").asText()); } catch (Exception ignored) {}
                }
                if (patientAccountId == null && dataObjectNode.has("metadata")) {
                    if (dataObjectNode.path("metadata").hasNonNull("patientAccountId")) {
                        try { patientAccountId = Long.parseLong(dataObjectNode.path("metadata").path("patientAccountId").asText()); } catch (Exception ignored) {}
                    } else if (dataObjectNode.path("metadata").hasNonNull("userId")) {
                        try { patientAccountId = Long.parseLong(dataObjectNode.path("metadata").path("userId").asText()); } catch (Exception ignored) {}
                    }
                }
            }

            if (psychologistAccountId == null && dataObjectNode.has("metadata") && dataObjectNode.path("metadata").hasNonNull("psychologistAccountId")) {
                try { psychologistAccountId = Long.parseLong(dataObjectNode.path("metadata").path("psychologistAccountId").asText()); } catch (Exception ignored) {}
            }

            if (appointmentId == null && dataObjectNode.has("metadata") && dataObjectNode.path("metadata").hasNonNull("appointmentId")) {
                try { appointmentId = Long.parseLong(dataObjectNode.path("metadata").path("appointmentId").asText()); } catch (Exception ignored) {}
            }

            if (plan == null && dataObjectNode.has("metadata") && dataObjectNode.path("metadata").hasNonNull("plan")) {
                try { plan = SubscriptionPlan.fromString(dataObjectNode.path("metadata").path("plan").asText()); } catch (Exception ignored) {}
            }

            if (dataObjectNode.has("metadata") && dataObjectNode.path("metadata").hasNonNull("type")) {
                paymentType = dataObjectNode.path("metadata").path("type").asText();
            }

            if (stripeSubId == null) {
                stripeSubId = dataObjectNode.path("subscription").asText(null);
                if (stripeSubId == null && "customer.subscription.deleted".equalsIgnoreCase(eventType)) {
                    stripeSubId = dataObjectNode.path("id").asText(null);
                }
            }

            if (stripeCustomerId == null) {
                stripeCustomerId = dataObjectNode.path("customer").asText(null);
            }

            if (subscriptionStatus == null && dataObjectNode.hasNonNull("status")) {
                subscriptionStatus = dataObjectNode.path("status").asText();
            }

            if (periodEnd == null && dataObjectNode.hasNonNull("current_period_end")) {
                long epochSec = dataObjectNode.path("current_period_end").asLong();
                if (epochSec > 0) {
                    periodEnd = Instant.ofEpochSecond(epochSec);
                }
            }

            if (chargesEnabled == null && dataObjectNode.has("charges_enabled")) {
                chargesEnabled = dataObjectNode.path("charges_enabled").asBoolean();
            }
            if (payoutsEnabled == null && dataObjectNode.has("payouts_enabled")) {
                payoutsEnabled = dataObjectNode.path("payouts_enabled").asBoolean();
            }
            if (detailsSubmitted == null && dataObjectNode.has("details_submitted")) {
                detailsSubmitted = dataObjectNode.path("details_submitted").asBoolean();
            }
            if (connectedAccount == null && "account.updated".equalsIgnoreCase(eventType)) {
                connectedAccount = dataObjectNode.path("id").asText(null);
            }

            if (payoutId == null && (eventType != null && eventType.startsWith("payout."))) {
                payoutId = dataObjectNode.path("id").asText(null);
                payoutAmountCents = dataObjectNode.path("amount").asLong(0L);
                payoutCurrency = dataObjectNode.path("currency").asText("usd");
                payoutStatus = dataObjectNode.path("status").asText();
            }

            return new StripeWebhookEvent(
                    eventId,
                    eventType,
                    patientAccountId,
                    plan,
                    periodEnd,
                    stripeSubId,
                    stripeCustomerId,
                    connectedAccount,
                    paymentType,
                    psychologistAccountId,
                    appointmentId,
                    paymentIntentId,
                    chargesEnabled,
                    payoutsEnabled,
                    detailsSubmitted,
                    payoutId,
                    payoutAmountCents,
                    payoutCurrency,
                    payoutStatus,
                    subscriptionStatus
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
