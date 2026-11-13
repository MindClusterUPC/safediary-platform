package com.mindcluster.safediary.payments.application.internal.commandservices;

import com.mindcluster.safediary.carescheduling.interfaces.acl.CareSchedulingContextFacade;
import com.mindcluster.safediary.cliniciandirectory.interfaces.acl.ClinicianDirectoryContextFacade;
import com.mindcluster.safediary.payments.application.commandservices.ConsultationPaymentCommandService;
import com.mindcluster.safediary.payments.application.commandservices.dto.ConsultationCheckoutResultDto;
import com.mindcluster.safediary.payments.application.outbound.StripeGatewayClient;
import com.mindcluster.safediary.payments.application.outbound.dto.StripeCheckoutSession;
import com.mindcluster.safediary.payments.domain.model.aggregates.ConnectedAccount;
import com.mindcluster.safediary.payments.domain.model.aggregates.PaymentIntent;
import com.mindcluster.safediary.payments.domain.model.commands.CreateConsultationCheckoutCommand;
import com.mindcluster.safediary.payments.domain.repositories.ConnectedAccountRepository;
import com.mindcluster.safediary.payments.domain.repositories.PaymentIntentRepository;
import com.mindcluster.safediary.payments.infrastructure.configuration.StripeProperties;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ConsultationPaymentCommandServiceImpl implements ConsultationPaymentCommandService {

    private static final Logger log = LoggerFactory.getLogger(ConsultationPaymentCommandServiceImpl.class);

    private final ConnectedAccountRepository connectedAccountRepository;
    private final PaymentIntentRepository paymentIntentRepository;
    private final StripeGatewayClient stripeGatewayClient;
    private final StripeProperties stripeProperties;
    private final CareSchedulingContextFacade careScheduling;
    private final ClinicianDirectoryContextFacade clinicianDirectory;

    public ConsultationPaymentCommandServiceImpl(ConnectedAccountRepository connectedAccountRepository,
                                                PaymentIntentRepository paymentIntentRepository,
                                                StripeGatewayClient stripeGatewayClient,
                                                StripeProperties stripeProperties,
                                                CareSchedulingContextFacade careScheduling,
                                                ClinicianDirectoryContextFacade clinicianDirectory) {
        this.connectedAccountRepository = connectedAccountRepository;
        this.paymentIntentRepository = paymentIntentRepository;
        this.stripeGatewayClient = stripeGatewayClient;
        this.stripeProperties = stripeProperties;
        this.careScheduling = careScheduling;
        this.clinicianDirectory = clinicianDirectory;
    }

    @Override
    @Transactional
    public Result<ConsultationCheckoutResultDto, ApplicationError> handle(CreateConsultationCheckoutCommand command) {
        try {
            var appointment = careScheduling.fetchPayableAppointment(command.appointmentId(), command.patientAccountId());
            if (appointment.isEmpty()) {
                return Result.failure(ApplicationError.businessRuleViolation("appointment-payment",
                        "The appointment has no active reservation waiting for your payment."));
            }
            var payable = appointment.get();

            var psychologistAccount = clinicianDirectory.fetchClinicianAccountId(payable.clinicianId());
            if (psychologistAccount.isEmpty()) {
                return Result.failure(ApplicationError.businessRuleViolation("PsychologistOnboarding",
                        "The psychologist is no longer available in the directory."));
            }
            Long psychologistAccountId = psychologistAccount.get();
            var accountOpt = connectedAccountRepository.findByPsychologistAccountId(psychologistAccountId);

            if (accountOpt.isEmpty()) {
                return Result.failure(ApplicationError.businessRuleViolation("PsychologistOnboarding",
                        "Psychologist has not created a connected Stripe account yet."));
            }

            ConnectedAccount connectedAccount = accountOpt.get();
            // If API key is configured, verify that charges are enabled on Stripe
            if (stripeProperties.getApiKey() != null && !stripeProperties.getApiKey().isBlank()
                    && !connectedAccount.isChargesEnabled()) {
                return Result.failure(ApplicationError.businessRuleViolation("PsychologistOnboarding",
                        "Psychologist has not completed onboarding; charges are not enabled yet."));
            }

            double feePercent = stripeProperties.getMarketplace() != null
                    ? stripeProperties.getMarketplace().getFeePercent()
                    : 15.0;
            BigDecimal platformFee = payable.amount()
                    .multiply(BigDecimal.valueOf(feePercent / 100.0))
                    .setScale(2, RoundingMode.HALF_UP);

            // Same reference Care Scheduling attached to the hold, so the webhook result matches the reservation.
            String idempotencyKey = payable.paymentReference();

            paymentIntentRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseGet(() -> paymentIntentRepository.save(new PaymentIntent(
                            "CONSULTATION",
                            payable.appointmentId(),
                            payable.patientAccountId(),
                            payable.amount(),
                            payable.currency(),
                            idempotencyKey
                    )));

            StripeCheckoutSession session = stripeGatewayClient.createConsultationCheckoutSession(
                    payable.patientAccountId(),
                    psychologistAccountId,
                    connectedAccount.getStripeAccountId(),
                    payable.amount(),
                    platformFee,
                    payable.currency(),
                    payable.appointmentId(),
                    command.successUrl(),
                    command.cancelUrl(),
                    idempotencyKey
            );

            return Result.success(new ConsultationCheckoutResultDto(
                    payable.appointmentId(),
                    payable.patientAccountId(),
                    psychologistAccountId,
                    payable.amount(),
                    payable.currency(),
                    session.checkoutUrl(),
                    session.sessionId()
            ));
        } catch (IllegalArgumentException ex) {
            log.warn("Validation error creating consultation checkout: {}", ex.getMessage());
            return Result.failure(ApplicationError.validationError("consultation-checkout", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error creating consultation checkout", ex);
            return Result.failure(ApplicationError.unexpected("consultation-checkout", ex.getMessage()));
        }
    }
}
