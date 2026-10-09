package com.mindcluster.safediary.payments.application;

import com.mindcluster.safediary.payments.application.internal.commandservices.SubscriptionCommandServiceImpl;
import com.mindcluster.safediary.payments.application.outbound.StripeGatewayClient;
import com.mindcluster.safediary.payments.application.outbound.dto.StripeCheckoutSession;
import com.mindcluster.safediary.payments.application.outbound.dto.StripeWebhookEvent;
import com.mindcluster.safediary.payments.domain.model.aggregates.Subscription;
import com.mindcluster.safediary.payments.domain.model.commands.CancelSubscriptionCommand;
import com.mindcluster.safediary.payments.domain.model.commands.CreateSubscriptionCheckoutCommand;
import com.mindcluster.safediary.payments.domain.model.commands.ProcessStripeWebhookCommand;
import com.mindcluster.safediary.payments.domain.model.entities.ProcessedWebhookEvent;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionStatus;
import com.mindcluster.safediary.payments.domain.repositories.ProcessedWebhookEventRepository;
import com.mindcluster.safediary.payments.domain.repositories.SubscriptionRepository;
import com.mindcluster.safediary.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SubscriptionCommandServiceTest {

    private SubscriptionRepository subscriptionRepository;
    private ProcessedWebhookEventRepository processedWebhookEventRepository;
    private StripeGatewayClient stripeGatewayClient;
    private SubscriptionCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        subscriptionRepository = Mockito.mock(SubscriptionRepository.class);
        processedWebhookEventRepository = Mockito.mock(ProcessedWebhookEventRepository.class);
        stripeGatewayClient = Mockito.mock(StripeGatewayClient.class);
        service = new SubscriptionCommandServiceImpl(subscriptionRepository, processedWebhookEventRepository, stripeGatewayClient);
    }

    @Test
    @DisplayName("Create checkout for FREE plan does not call Stripe and activates free plan directly")
    void testCreateFreePlanCheckout() {
        when(subscriptionRepository.findByPatientAccountId(100L)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        var command = new CreateSubscriptionCheckoutCommand(100L, SubscriptionPlan.FREE, null, null, null);
        var result = service.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        var dto = ((Result.Success<?, ?>) result).value();
        assertThat(dto).isNotNull();

        verify(stripeGatewayClient, never()).createSubscriptionCheckoutSession(any(), any(), any(), any(), any());
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Create checkout for TERRA calls Stripe gateway client and passes idempotency key")
    void testCreateTerraPlanCheckout() {
        when(subscriptionRepository.findByPatientAccountId(100L)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));
        when(stripeGatewayClient.createSubscriptionCheckoutSession(eq(100L), eq(SubscriptionPlan.TERRA), any(), any(), eq("custom-key-123")))
                .thenReturn(new StripeCheckoutSession("cs_test_session", "https://checkout.stripe.com/pay/cs_test_session"));

        var command = new CreateSubscriptionCheckoutCommand(100L, SubscriptionPlan.TERRA, "http://ok", "http://cancel", "custom-key-123");
        var result = service.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        verify(stripeGatewayClient).createSubscriptionCheckoutSession(eq(100L), eq(SubscriptionPlan.TERRA), eq("http://ok"), eq("http://cancel"), eq("custom-key-123"));
    }

    @Test
    @DisplayName("Process webhook handles events idempotently: duplicates are safely ignored")
    void testProcessWebhookIdempotency() {
        String eventId = "evt_stripe_test_456";
        String payload = "{\"id\":\"" + eventId + "\"}";
        String signature = "sig_valid";
        Instant periodEnd = Instant.now().plus(30, ChronoUnit.DAYS);

        var webhookEvent = new StripeWebhookEvent(
                eventId,
                "checkout.session.completed",
                100L,
                SubscriptionPlan.TERRA,
                periodEnd,
                "sub_123",
                "cus_abc"
        );

        when(stripeGatewayClient.parseAndVerifyWebhook(payload, signature)).thenReturn(webhookEvent);
        // First run: event does not exist yet in idempotency store
        when(processedWebhookEventRepository.existsByEventId(eventId)).thenReturn(false);
        when(subscriptionRepository.findByPatientAccountId(100L)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        var command = new ProcessStripeWebhookCommand(payload, signature);
        var firstResult = service.handle(command);

        assertThat(firstResult).isInstanceOf(Result.Success.class);
        verify(processedWebhookEventRepository).save(any(ProcessedWebhookEvent.class));
        verify(subscriptionRepository).save(any(Subscription.class));

        // Second run with identical event ID: idempotency check returns true
        when(processedWebhookEventRepository.existsByEventId(eventId)).thenReturn(true);
        reset(subscriptionRepository);

        var secondResult = service.handle(command);

        assertThat(secondResult).isInstanceOf(Result.Success.class);
        verify(subscriptionRepository, never()).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Process webhook fails with UNAUTHORIZED when signature is rejected")
    void testProcessWebhookSignatureFailure() {
        String payload = "{\"id\":\"evt_fake\"}";
        String signature = "sig_invalid";

        when(stripeGatewayClient.parseAndVerifyWebhook(payload, signature))
                .thenThrow(new SecurityException("Signature verification failed"));

        var command = new ProcessStripeWebhookCommand(payload, signature);
        var result = service.handle(command);

        assertThat(result).isInstanceOf(Result.Failure.class);
        var failure = (Result.Failure<?, com.mindcluster.safediary.shared.application.result.ApplicationError>) result;
        assertThat(failure.error().code()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    @DisplayName("Cancel subscription marks subscription as CANCELLED and contacts Stripe")
    void testCancelSubscription() {
        Subscription existing = new Subscription(1L, 100L, SubscriptionPlan.TERRA, SubscriptionStatus.ACTIVE, Instant.now().plus(10, ChronoUnit.DAYS), "sub_remote_1", "cus_1");
        when(subscriptionRepository.findByPatientAccountId(100L)).thenReturn(Optional.of(existing));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

        var command = new CancelSubscriptionCommand(100L);
        var result = service.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        verify(stripeGatewayClient).cancelSubscription("sub_remote_1");
        assertThat(existing.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
    }
}
