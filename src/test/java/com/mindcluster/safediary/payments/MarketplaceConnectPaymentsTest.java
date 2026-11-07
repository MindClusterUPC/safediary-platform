package com.mindcluster.safediary.payments;

import com.mindcluster.safediary.carescheduling.interfaces.acl.dto.AppointmentPaymentResultDto;
import com.mindcluster.safediary.payments.application.commandservices.dto.ConnectedAccountOnboardingDto;
import com.mindcluster.safediary.payments.application.commandservices.dto.ConsultationCheckoutResultDto;
import com.mindcluster.safediary.payments.application.internal.commandservices.ConnectedAccountCommandServiceImpl;
import com.mindcluster.safediary.payments.application.internal.commandservices.ConsultationPaymentCommandServiceImpl;
import com.mindcluster.safediary.payments.application.internal.commandservices.SubscriptionCommandServiceImpl;
import com.mindcluster.safediary.payments.application.outbound.StripeGatewayClient;
import com.mindcluster.safediary.payments.application.outbound.dto.StripeCheckoutSession;
import com.mindcluster.safediary.payments.application.outbound.dto.StripeWebhookEvent;
import com.mindcluster.safediary.payments.domain.model.aggregates.ConnectedAccount;
import com.mindcluster.safediary.payments.domain.model.aggregates.PaymentIntent;
import com.mindcluster.safediary.payments.domain.model.aggregates.Subscription;
import com.mindcluster.safediary.payments.domain.model.commands.CreateConnectedAccountCommand;
import com.mindcluster.safediary.payments.domain.model.commands.CreateConsultationCheckoutCommand;
import com.mindcluster.safediary.payments.domain.model.commands.ProcessStripeWebhookCommand;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionStatus;
import com.mindcluster.safediary.payments.domain.repositories.ConnectedAccountRepository;
import com.mindcluster.safediary.payments.domain.repositories.PaymentIntentRepository;
import com.mindcluster.safediary.payments.domain.repositories.ProcessedWebhookEventRepository;
import com.mindcluster.safediary.payments.domain.repositories.SubscriptionRepository;
import com.mindcluster.safediary.payments.infrastructure.adapters.StripeGatewayAclAdapter;
import com.mindcluster.safediary.payments.infrastructure.configuration.StripeProperties;
import com.mindcluster.safediary.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MarketplaceConnectPaymentsTest {

    private ConnectedAccountRepository connectedAccountRepository;
    private PaymentIntentRepository paymentIntentRepository;
    private SubscriptionRepository subscriptionRepository;
    private ProcessedWebhookEventRepository processedWebhookEventRepository;
    private StripeGatewayClient stripeGatewayClient;
    private ApplicationEventPublisher eventPublisher;
    private StripeProperties stripeProperties;

    @BeforeEach
    void setUp() {
        connectedAccountRepository = mock(ConnectedAccountRepository.class);
        paymentIntentRepository = mock(PaymentIntentRepository.class);
        subscriptionRepository = mock(SubscriptionRepository.class);
        processedWebhookEventRepository = mock(ProcessedWebhookEventRepository.class);
        stripeGatewayClient = mock(StripeGatewayClient.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        stripeProperties = new StripeProperties();
    }

    @Test
    @DisplayName("Psychologist onboarding: creates Express connected account and returns AccountLink")
    void testPsychologistOnboardingFlow() {
        ConnectedAccountCommandServiceImpl service =
                new ConnectedAccountCommandServiceImpl(connectedAccountRepository, stripeGatewayClient);

        when(connectedAccountRepository.findByPsychologistAccountId(200L)).thenReturn(Optional.empty());
        when(stripeGatewayClient.createConnectedAccount(200L, "dr.smith@example.com")).thenReturn("acct_smith_123");
        when(connectedAccountRepository.save(any(ConnectedAccount.class))).thenAnswer(i -> i.getArgument(0));
        when(stripeGatewayClient.createAccountLink(eq("acct_smith_123"), any(), any()))
                .thenReturn("https://connect.stripe.com/setup/s/xyz");

        var command = new CreateConnectedAccountCommand(200L, "dr.smith@example.com", "http://refresh", "http://return");
        var result = service.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        ConnectedAccountOnboardingDto dto = ((Result.Success<ConnectedAccountOnboardingDto, ?>) result).value();
        assertThat(dto.psychologistAccountId()).isEqualTo(200L);
        assertThat(dto.stripeAccountId()).isEqualTo("acct_smith_123");
        assertThat(dto.onboardingUrl()).isEqualTo("https://connect.stripe.com/setup/s/xyz");

        verify(connectedAccountRepository).save(any(ConnectedAccount.class));
    }

    @Test
    @DisplayName("Consultation split payment: validates connected account and creates destination charge session")
    void testConsultationSplitPayment() {
        ConsultationPaymentCommandServiceImpl service = new ConsultationPaymentCommandServiceImpl(
                connectedAccountRepository, paymentIntentRepository, stripeGatewayClient, stripeProperties);

        ConnectedAccount readyAccount = new ConnectedAccount(1L, 200L, "acct_smith_123", true, true, true, Instant.now(), Instant.now());
        when(connectedAccountRepository.findByPsychologistAccountId(200L)).thenReturn(Optional.of(readyAccount));
        when(paymentIntentRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(paymentIntentRepository.save(any(PaymentIntent.class))).thenAnswer(i -> i.getArgument(0));
        when(stripeGatewayClient.createConsultationCheckoutSession(
                eq(100L), eq(200L), eq("acct_smith_123"), any(), any(), eq("USD"), eq(555L), any(), any(), any()))
                .thenReturn(new StripeCheckoutSession("cs_consultation_test", "https://checkout.stripe.com/pay/cs_consultation_test"));

        var command = new CreateConsultationCheckoutCommand(
                100L, 200L, 555L, new BigDecimal("60.00"), new BigDecimal("9.00"), "USD",
                "http://ok", "http://cancel", "idemp-consultation-555");
        var result = service.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        ConsultationCheckoutResultDto dto = ((Result.Success<ConsultationCheckoutResultDto, ?>) result).value();
        assertThat(dto.appointmentId()).isEqualTo(555L);
        assertThat(dto.sessionId()).isEqualTo("cs_consultation_test");
        assertThat(dto.checkoutUrl()).isEqualTo("https://checkout.stripe.com/pay/cs_consultation_test");

        verify(paymentIntentRepository).save(any(PaymentIntent.class));
        verify(stripeGatewayClient).createConsultationCheckoutSession(
                eq(100L), eq(200L), eq("acct_smith_123"), eq(new BigDecimal("60.00")), eq(new BigDecimal("9.00")),
                eq("USD"), eq(555L), eq("http://ok"), eq("http://cancel"), eq("idemp-consultation-555"));
    }

    @Test
    @DisplayName("Webhook account.updated updates chargesEnabled and payoutsEnabled on ConnectedAccount")
    void testWebhookAccountUpdated() {
        SubscriptionCommandServiceImpl service = new SubscriptionCommandServiceImpl(
                subscriptionRepository, processedWebhookEventRepository, stripeGatewayClient,
                connectedAccountRepository, paymentIntentRepository, eventPublisher);

        String eventId = "evt_account_updated_1";
        String payload = "{\"id\":\"" + eventId + "\"}";
        String sig = "sig_valid";

        StripeWebhookEvent event = new StripeWebhookEvent(
                eventId,
                "account.updated",
                null,
                null,
                null,
                null,
                null,
                "acct_smith_123",
                null,
                200L,
                null,
                null,
                true,
                true,
                true,
                null,
                null,
                null,
                null,
                null
        );

        when(stripeGatewayClient.parseAndVerifyWebhook(payload, sig)).thenReturn(event);
        when(processedWebhookEventRepository.existsByEventId(eventId)).thenReturn(false);

        ConnectedAccount account = new ConnectedAccount(1L, 200L, "acct_smith_123", false, false, false, Instant.now(), Instant.now());
        when(connectedAccountRepository.findByStripeAccountId("acct_smith_123")).thenReturn(Optional.of(account));

        var command = new ProcessStripeWebhookCommand(payload, sig);
        var result = service.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        assertThat(account.isChargesEnabled()).isTrue();
        assertThat(account.isPayoutsEnabled()).isTrue();
        assertThat(account.isDetailsSubmitted()).isTrue();
        assertThat(account.isReady()).isTrue();
        verify(connectedAccountRepository).save(account);
    }

    @Test
    @DisplayName("Webhook checkout.session.completed for CONSULTATION marks PaymentIntent and publishes event for CareScheduling")
    void testWebhookConsultationCompleted() {
        SubscriptionCommandServiceImpl service = new SubscriptionCommandServiceImpl(
                subscriptionRepository, processedWebhookEventRepository, stripeGatewayClient,
                connectedAccountRepository, paymentIntentRepository, eventPublisher);

        String eventId = "evt_consultation_completed_2";
        String payload = "{\"id\":\"" + eventId + "\"}";
        String sig = "sig_valid";

        StripeWebhookEvent event = new StripeWebhookEvent(
                eventId,
                "checkout.session.completed",
                100L,
                null,
                null,
                null,
                "cus_pat_1",
                null,
                "CONSULTATION",
                200L,
                777L,
                "pi_stripe_12345",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        when(stripeGatewayClient.parseAndVerifyWebhook(payload, sig)).thenReturn(event);
        when(processedWebhookEventRepository.existsByEventId(eventId)).thenReturn(false);

        PaymentIntent pi = new PaymentIntent("CONSULTATION", 777L, 100L, new BigDecimal("60.00"), "USD", "care-appointment-777");
        when(paymentIntentRepository.findByIdempotencyKey("care-appointment-777")).thenReturn(Optional.of(pi));

        var command = new ProcessStripeWebhookCommand(payload, sig);
        var result = service.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        assertThat(pi.getStatus()).isEqualTo("APPROVED");
        assertThat(pi.getProviderReference()).isEqualTo("pi_stripe_12345");
        verify(paymentIntentRepository).save(pi);

        ArgumentCaptor<AppointmentPaymentResultDto> captor = ArgumentCaptor.forClass(AppointmentPaymentResultDto.class);
        verify(eventPublisher).publishEvent(captor.capture());
        AppointmentPaymentResultDto published = captor.getValue();
        assertThat(published.appointmentId()).isEqualTo(777L);
        assertThat(published.approved()).isTrue();
        assertThat(published.paymentReference()).isEqualTo("pi_stripe_12345");
    }

    @Test
    @DisplayName("Webhook customer.subscription.deleted degrades patient subscription to FREE in database")
    void testWebhookSubscriptionDeletedDowngradesToFree() {
        SubscriptionCommandServiceImpl service = new SubscriptionCommandServiceImpl(
                subscriptionRepository, processedWebhookEventRepository, stripeGatewayClient,
                connectedAccountRepository, paymentIntentRepository, eventPublisher);

        String eventId = "evt_sub_deleted_3";
        String payload = "{\"id\":\"" + eventId + "\"}";
        String sig = "sig_valid";

        StripeWebhookEvent event = new StripeWebhookEvent(
                eventId,
                "customer.subscription.deleted",
                100L,
                SubscriptionPlan.TERRA,
                Instant.now(),
                "sub_123",
                "cus_abc"
        );

        when(stripeGatewayClient.parseAndVerifyWebhook(payload, sig)).thenReturn(event);
        when(processedWebhookEventRepository.existsByEventId(eventId)).thenReturn(false);

        Subscription activeTerra = new Subscription(1L, 100L, SubscriptionPlan.TERRA, SubscriptionStatus.ACTIVE,
                Instant.now().plus(20, ChronoUnit.DAYS), "sub_123", "cus_abc");
        when(subscriptionRepository.findByPatientAccountId(100L)).thenReturn(Optional.of(activeTerra));

        var command = new ProcessStripeWebhookCommand(payload, sig);
        var result = service.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        assertThat(activeTerra.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(activeTerra.getPlan()).isEqualTo(SubscriptionPlan.FREE);
        assertThat(activeTerra.getActiveUntil()).isNull();
        assertThat(activeTerra.getStripeSubscriptionId()).isNull();
        verify(subscriptionRepository).save(activeTerra);
    }
}
