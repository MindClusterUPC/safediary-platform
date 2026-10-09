package com.mindcluster.safediary.payments.application.internal.commandservices;

import com.mindcluster.safediary.payments.application.commandservices.SubscriptionCommandService;
import com.mindcluster.safediary.payments.application.commandservices.dto.SubscriptionCheckoutResultDto;
import com.mindcluster.safediary.payments.application.outbound.StripeGatewayClient;
import com.mindcluster.safediary.payments.application.outbound.dto.StripeCheckoutSession;
import com.mindcluster.safediary.payments.application.outbound.dto.StripeWebhookEvent;
import com.mindcluster.safediary.payments.domain.model.aggregates.Subscription;
import com.mindcluster.safediary.payments.domain.model.commands.CancelSubscriptionCommand;
import com.mindcluster.safediary.payments.domain.model.commands.CreateSubscriptionCheckoutCommand;
import com.mindcluster.safediary.payments.domain.model.commands.ProcessStripeWebhookCommand;
import com.mindcluster.safediary.payments.domain.model.entities.ProcessedWebhookEvent;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;
import com.mindcluster.safediary.payments.domain.repositories.ProcessedWebhookEventRepository;
import com.mindcluster.safediary.payments.domain.repositories.SubscriptionRepository;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class SubscriptionCommandServiceImpl implements SubscriptionCommandService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionCommandServiceImpl.class);

    private final SubscriptionRepository subscriptionRepository;
    private final ProcessedWebhookEventRepository processedWebhookEventRepository;
    private final StripeGatewayClient stripeGatewayClient;

    public SubscriptionCommandServiceImpl(SubscriptionRepository subscriptionRepository,
                                         ProcessedWebhookEventRepository processedWebhookEventRepository,
                                         StripeGatewayClient stripeGatewayClient) {
        this.subscriptionRepository = subscriptionRepository;
        this.processedWebhookEventRepository = processedWebhookEventRepository;
        this.stripeGatewayClient = stripeGatewayClient;
    }

    @Override
    @Transactional
    public Result<SubscriptionCheckoutResultDto, ApplicationError> handle(CreateSubscriptionCheckoutCommand command) {
        try {
            Long patientAccountId = command.patientAccountId();
            SubscriptionPlan requestedPlan = command.plan() == null ? SubscriptionPlan.FREE : command.plan();

            if (requestedPlan == SubscriptionPlan.FREE) {
                Subscription subscription = subscriptionRepository.findByPatientAccountId(patientAccountId)
                        .orElseGet(() -> Subscription.createFree(patientAccountId));
                subscription.assignFreePlan();
                subscriptionRepository.save(subscription);

                return Result.success(new SubscriptionCheckoutResultDto(
                        patientAccountId,
                        SubscriptionPlan.FREE,
                        null,
                        null,
                        false,
                        "Free plan activated. No payment or checkout required."
                ));
            }

            String idempotencyKey = (command.idempotencyKey() != null && !command.idempotencyKey().isBlank())
                    ? command.idempotencyKey().trim()
                    : UUID.randomUUID().toString();

            Subscription subscription = subscriptionRepository.findByPatientAccountId(patientAccountId)
                    .orElseGet(() -> Subscription.createFree(patientAccountId));
            subscription.markPendingCheckout(requestedPlan, null);
            subscriptionRepository.save(subscription);

            StripeCheckoutSession session = stripeGatewayClient.createSubscriptionCheckoutSession(
                    patientAccountId,
                    requestedPlan,
                    command.successUrl(),
                    command.cancelUrl(),
                    idempotencyKey
            );

            return Result.success(new SubscriptionCheckoutResultDto(
                    patientAccountId,
                    requestedPlan,
                    session.checkoutUrl(),
                    session.sessionId(),
                    true,
                    "Checkout session created successfully."
            ));
        } catch (IllegalArgumentException ex) {
            log.warn("Validation error creating subscription checkout: {}", ex.getMessage());
            return Result.failure(ApplicationError.validationError("subscription-checkout", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error creating subscription checkout", ex);
            return Result.failure(ApplicationError.unexpected("subscription-checkout", ex.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<Subscription, ApplicationError> handle(CancelSubscriptionCommand command) {
        try {
            Long patientAccountId = command.patientAccountId();
            var optionalSub = subscriptionRepository.findByPatientAccountId(patientAccountId);
            if (optionalSub.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Subscription", "No subscription found for patient ID " + patientAccountId));
            }

            Subscription subscription = optionalSub.get();
            if (subscription.getStripeSubscriptionId() != null && !subscription.getStripeSubscriptionId().isBlank()) {
                try {
                    stripeGatewayClient.cancelSubscription(subscription.getStripeSubscriptionId());
                } catch (Exception ex) {
                    log.warn("Could not cancel remote Stripe subscription {}: {}", subscription.getStripeSubscriptionId(), ex.getMessage());
                }
            }

            subscription.cancel();
            Subscription saved = subscriptionRepository.save(subscription);
            return Result.success(saved);
        } catch (Exception ex) {
            log.error("Unexpected error cancelling subscription", ex);
            return Result.failure(ApplicationError.unexpected("subscription-cancel", ex.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<Void, ApplicationError> handle(ProcessStripeWebhookCommand command) {
        try {
            StripeWebhookEvent event = stripeGatewayClient.parseAndVerifyWebhook(
                    command.payloadJson(),
                    command.stripeSignatureHeader()
            );

            // Strict Idempotency Check: if this Stripe event has already been recorded, ignore duplicate safely.
            if (processedWebhookEventRepository.existsByEventId(event.eventId())) {
                log.info("Duplicate Stripe event {} already processed. Skipping idempotently.", event.eventId());
                return Result.success(null);
            }

            // Record processed event immediately to maintain idempotency
            processedWebhookEventRepository.save(new ProcessedWebhookEvent(
                    event.eventId(),
                    event.eventType(),
                    Instant.now()
            ));

            if (event.patientAccountId() != null) {
                Long patientId = event.patientAccountId();
                Subscription subscription = subscriptionRepository.findByPatientAccountId(patientId)
                        .orElseGet(() -> Subscription.createFree(patientId));

                if ("customer.subscription.deleted".equalsIgnoreCase(event.eventType())) {
                    subscription.cancel();
                    subscriptionRepository.save(subscription);
                    log.info("Subscription for patient {} cancelled via webhook.", patientId);
                } else {
                    SubscriptionPlan plan = event.plan() != null ? event.plan() : SubscriptionPlan.TERRA;
                    Instant activeUntil = event.periodEnd();
                    if (activeUntil == null) {
                        activeUntil = plan == SubscriptionPlan.ASTRUM
                                ? Instant.now().plus(365, ChronoUnit.DAYS)
                                : Instant.now().plus(30, ChronoUnit.DAYS);
                    }
                    subscription.activateSubscription(
                            plan,
                            activeUntil,
                            event.stripeSubscriptionId(),
                            event.stripeCustomerId()
                    );
                    subscriptionRepository.save(subscription);
                    log.info("Subscription for patient {} activated to plan {} until {}.", patientId, plan, activeUntil);
                }
            }

            return Result.success(null);
        } catch (SecurityException ex) {
            log.warn("Stripe webhook cryptographic signature rejected: {}", ex.getMessage());
            return Result.failure(new ApplicationError("UNAUTHORIZED", "Stripe webhook signature is invalid", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            log.warn("Malformed Stripe webhook payload: {}", ex.getMessage());
            return Result.failure(ApplicationError.validationError("stripe-webhook", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error handling Stripe webhook", ex);
            return Result.failure(ApplicationError.unexpected("stripe-webhook", ex.getMessage()));
        }
    }
}
