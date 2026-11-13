package com.mindcluster.safediary.payments;

import com.mindcluster.safediary.carescheduling.interfaces.acl.CareSchedulingContextFacade;
import com.mindcluster.safediary.carescheduling.interfaces.acl.dto.AppointmentPaymentResultDto;
import com.mindcluster.safediary.carescheduling.interfaces.acl.dto.PayableAppointmentDto;
import com.mindcluster.safediary.cliniciandirectory.interfaces.acl.ClinicianDirectoryContextFacade;
import com.mindcluster.safediary.payments.application.commandservices.dto.SkipConsultationPaymentResultDto;
import com.mindcluster.safediary.payments.application.internal.commandservices.ConsultationPaymentCommandServiceImpl;
import com.mindcluster.safediary.payments.application.internal.queryservices.PatientPaymentsQueryServiceImpl;
import com.mindcluster.safediary.payments.application.outbound.StripeGatewayClient;
import com.mindcluster.safediary.payments.domain.model.aggregates.PaymentIntent;
import com.mindcluster.safediary.payments.domain.model.commands.SkipConsultationPaymentCommand;
import com.mindcluster.safediary.payments.domain.repositories.ConnectedAccountRepository;
import com.mindcluster.safediary.payments.domain.repositories.PaymentIntentRepository;
import com.mindcluster.safediary.payments.domain.repositories.SubscriptionRepository;
import com.mindcluster.safediary.payments.infrastructure.configuration.StripeProperties;
import com.mindcluster.safediary.payments.interfaces.rest.resources.PaymentHistoryItemResource;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConsultationSkipPaymentAndHistoryTest {

    private ConnectedAccountRepository connectedAccountRepository;
    private PaymentIntentRepository paymentIntentRepository;
    private SubscriptionRepository subscriptionRepository;
    private StripeGatewayClient stripeGatewayClient;
    private StripeProperties stripeProperties;
    private CareSchedulingContextFacade careScheduling;
    private ClinicianDirectoryContextFacade clinicianDirectory;
    private ConsultationPaymentCommandServiceImpl commandService;
    private PatientPaymentsQueryServiceImpl queryService;

    @BeforeEach
    void setUp() {
        connectedAccountRepository = mock(ConnectedAccountRepository.class);
        paymentIntentRepository = mock(PaymentIntentRepository.class);
        subscriptionRepository = mock(SubscriptionRepository.class);
        stripeGatewayClient = mock(StripeGatewayClient.class);
        stripeProperties = new StripeProperties();
        careScheduling = mock(CareSchedulingContextFacade.class);
        clinicianDirectory = mock(ClinicianDirectoryContextFacade.class);

        commandService = new ConsultationPaymentCommandServiceImpl(
                connectedAccountRepository,
                paymentIntentRepository,
                stripeGatewayClient,
                stripeProperties,
                careScheduling,
                clinicianDirectory,
                subscriptionRepository
        );
        queryService = new PatientPaymentsQueryServiceImpl(
                paymentIntentRepository,
                subscriptionRepository,
                stripeGatewayClient,
                null
        );
    }

    @Test
    @DisplayName("Skip consultation payment confirms held appointment and records SKIPPED_ONCE intent")
    void skipPaymentConfirmsAppointmentAndSavesIntent() {
        var payable = new PayableAppointmentDto(
                42L,
                5L,
                10L,
                new BigDecimal("120.00"),
                "PEN",
                "care-appointment-42",
                Instant.now().plusSeconds(600)
        );
        when(careScheduling.fetchPayableAppointment(42L, 5L)).thenReturn(Optional.of(payable));
        when(careScheduling.applyPaymentResult(any(AppointmentPaymentResultDto.class)))
                .thenReturn(Result.success("CONFIRMED"));
        when(paymentIntentRepository.findByIdempotencyKey("care-appointment-42")).thenReturn(Optional.empty());
        when(paymentIntentRepository.save(any(PaymentIntent.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = commandService.handle(new SkipConsultationPaymentCommand(5L, 42L));

        assertThat(result).isInstanceOf(Result.Success.class);
        SkipConsultationPaymentResultDto dto = ((Result.Success<SkipConsultationPaymentResultDto, ApplicationError>) result).value();
        assertThat(dto.appointmentStatus()).isEqualTo("CONFIRMED");
        assertThat(dto.reference()).isEqualTo("SKIPPED_ONCE");
        assertThat(dto.amount()).isEqualByComparingTo("120.00");
        assertThat(dto.currency()).isEqualTo("PEN");

        ArgumentCaptor<PaymentIntent> intentCaptor = ArgumentCaptor.forClass(PaymentIntent.class);
        verify(paymentIntentRepository).save(intentCaptor.capture());
        assertThat(intentCaptor.getValue().getStatus()).isEqualTo("APPROVED");
        assertThat(intentCaptor.getValue().getProviderReference()).isEqualTo("SKIPPED_ONCE");
    }

    @Test
    @DisplayName("Skip consultation payment fails cleanly when no active reservation exists")
    void skipPaymentFailsWhenNoActiveHold() {
        when(careScheduling.fetchPayableAppointment(42L, 5L)).thenReturn(Optional.empty());

        var result = commandService.handle(new SkipConsultationPaymentCommand(5L, 42L));

        assertThat(result).isInstanceOf(Result.Failure.class);
        verify(careScheduling, never()).applyPaymentResult(any());
        verify(paymentIntentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Skip consultation payment does not record APPROVED intent if appointment confirmation fails")
    void skipPaymentPropagatesConfirmationFailure() {
        var payable = new PayableAppointmentDto(
                42L, 5L, 10L, new BigDecimal("120.00"), "PEN", "care-appointment-42", Instant.now().plusSeconds(600)
        );
        when(careScheduling.fetchPayableAppointment(42L, 5L)).thenReturn(Optional.of(payable));
        when(careScheduling.applyPaymentResult(any()))
                .thenReturn(Result.failure(ApplicationError.businessRuleViolation("hold-expired", "Hold expired")));

        var result = commandService.handle(new SkipConsultationPaymentCommand(5L, 42L));

        assertThat(result).isInstanceOf(Result.Failure.class);
        verify(paymentIntentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Payment history filters unpaid intents, formats PEN currency, and distinguishes skipped payments")
    void paymentHistoryFormatsAndFiltersCorrectly() {
        Instant older = Instant.parse("2026-10-01T10:00:00Z");
        Instant newer = Instant.parse("2026-10-02T12:00:00Z");

        PaymentIntent abandoned = new PaymentIntent(1L, "CONSULTATION", 11L, 5L,
                new BigDecimal("100.00"), "PEN", "care-appointment-11", null, "CREATED", older);
        PaymentIntent realPaid = new PaymentIntent(2L, "CONSULTATION", 12L, 5L,
                new BigDecimal("150.00"), "PEN", "care-appointment-12", "pi_123", "APPROVED", older);
        PaymentIntent skipped = new PaymentIntent(3L, "CONSULTATION", 13L, 5L,
                new BigDecimal("120.00"), "PEN", "care-appointment-13", "SKIPPED_ONCE", "APPROVED", newer);

        when(paymentIntentRepository.findByPayerAccountId(5L)).thenReturn(List.of(abandoned, realPaid, skipped));
        when(subscriptionRepository.findByPatientAccountId(5L)).thenReturn(Optional.empty());

        List<PaymentHistoryItemResource> history = queryService.getTransactionHistory(5L);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).id()).isEqualTo("pay_3");
        assertThat(history.get(0).amountFormatted()).isEqualTo("-S/ 120.00");
        assertThat(history.get(0).referenceNumber()).contains("Pago omitido (Demo)");
        assertThat(history.get(1).id()).isEqualTo("pay_2");
        assertThat(history.get(1).amountFormatted()).isEqualTo("-S/ 150.00");
        assertThat(history.get(1).referenceNumber()).contains("Verificado ✓");
    }
}
