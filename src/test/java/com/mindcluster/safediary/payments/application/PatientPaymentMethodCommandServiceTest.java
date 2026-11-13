package com.mindcluster.safediary.payments.application;

import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.iam.interfaces.acl.dto.IamAccountDto;
import com.mindcluster.safediary.payments.application.commandservices.dto.PaymentMethodSetupResultDto;
import com.mindcluster.safediary.payments.application.internal.commandservices.PatientPaymentMethodCommandServiceImpl;
import com.mindcluster.safediary.payments.application.outbound.StripeGatewayClient;
import com.mindcluster.safediary.payments.application.outbound.dto.StripeCheckoutSession;
import com.mindcluster.safediary.payments.application.queryservices.PatientPaymentsQueryService;
import com.mindcluster.safediary.payments.domain.model.aggregates.Subscription;
import com.mindcluster.safediary.payments.domain.model.commands.CreatePaymentMethodSetupCommand;
import com.mindcluster.safediary.payments.domain.model.commands.SetDefaultPaymentMethodCommand;
import com.mindcluster.safediary.payments.domain.repositories.SubscriptionRepository;
import com.mindcluster.safediary.payments.infrastructure.configuration.StripeProperties;
import com.mindcluster.safediary.payments.interfaces.rest.resources.PaymentMethodResource;
import com.mindcluster.safediary.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PatientPaymentMethodCommandServiceTest {

    private SubscriptionRepository subscriptionRepository;
    private StripeGatewayClient stripeGatewayClient;
    private PatientPaymentsQueryService patientPaymentsQueryService;
    private StripeProperties stripeProperties;
    private IamContextFacade iamContextFacade;
    private PatientPaymentMethodCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        subscriptionRepository = mock(SubscriptionRepository.class);
        stripeGatewayClient = mock(StripeGatewayClient.class);
        patientPaymentsQueryService = mock(PatientPaymentsQueryService.class);
        stripeProperties = new StripeProperties();
        stripeProperties.setApiKey("sk_test_dummy");
        iamContextFacade = mock(IamContextFacade.class);
        service = new PatientPaymentMethodCommandServiceImpl(
                subscriptionRepository,
                stripeGatewayClient,
                patientPaymentsQueryService,
                stripeProperties,
                iamContextFacade
        );
    }

    @Test
    @DisplayName("Creates a Stripe customer when patient has none and starts Checkout setup session")
    void testCreatePaymentMethodSetupCreatesCustomerWhenMissing() {
        Subscription freeSub = Subscription.createFree(5L);
        when(subscriptionRepository.findByPatientAccountId(5L)).thenReturn(Optional.of(freeSub));
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));
        when(iamContextFacade.fetchAccount(5L))
                .thenReturn(Optional.of(new IamAccountDto(5L, "juan@wang.com", "PATIENT", "ACTIVE")));
        when(stripeGatewayClient.createCustomer(5L, "juan@wang.com")).thenReturn("cus_test_5");
        when(stripeGatewayClient.createPaymentMethodSetupSession(eq(5L), eq("cus_test_5"), any(), any(), any()))
                .thenReturn(new StripeCheckoutSession("cs_setup_1", "https://checkout.stripe.com/c/pay/cs_setup_1"));

        var result = service.handle(new CreatePaymentMethodSetupCommand(5L, null, null));

        assertThat(result).isInstanceOf(Result.Success.class);
        PaymentMethodSetupResultDto dto = ((Result.Success<PaymentMethodSetupResultDto, ?>) result).value();
        assertThat(dto.checkoutUrl()).isEqualTo("https://checkout.stripe.com/c/pay/cs_setup_1");
        assertThat(dto.sessionId()).isEqualTo("cs_setup_1");
        assertThat(freeSub.getStripeCustomerId()).isEqualTo("cus_test_5");
        verify(stripeGatewayClient).createCustomer(5L, "juan@wang.com");
        verify(stripeGatewayClient).createPaymentMethodSetupSession(eq(5L), eq("cus_test_5"), any(), any(), any());
    }

    @Test
    @DisplayName("Reuses existing Stripe customer and persists default payment method updates")
    void testReusesExistingCustomerAndSetsDefaultMethod() {
        Subscription existing = Subscription.createFree(5L);
        existing.assignStripeCustomerId("cus_existing_99");
        when(subscriptionRepository.findByPatientAccountId(5L)).thenReturn(Optional.of(existing));
        when(stripeGatewayClient.createPaymentMethodSetupSession(eq(5L), eq("cus_existing_99"), any(), any(), any()))
                .thenReturn(new StripeCheckoutSession("cs_setup_2", "https://checkout.stripe.com/c/pay/cs_setup_2"));

        var setupResult = service.handle(new CreatePaymentMethodSetupCommand(5L, null, null));
        assertThat(setupResult).isInstanceOf(Result.Success.class);
        verify(stripeGatewayClient, never()).createCustomer(any(), any());

        List<PaymentMethodResource> updated = List.of(
                new PaymentMethodResource("pm_2", "MASTERCARD", "5555", "08/29", "Juan Wang", true, "Billetera SafeDiary Activa")
        );
        when(patientPaymentsQueryService.getSavedPaymentMethods(5L)).thenReturn(updated);

        var defaultResult = service.handle(new SetDefaultPaymentMethodCommand(5L, "pm_2"));
        assertThat(defaultResult).isInstanceOf(Result.Success.class);
        verify(stripeGatewayClient).setDefaultCustomerPaymentMethod("cus_existing_99", "pm_2");
        assertThat(((Result.Success<List<PaymentMethodResource>, ?>) defaultResult).value()).isEqualTo(updated);
    }
}
