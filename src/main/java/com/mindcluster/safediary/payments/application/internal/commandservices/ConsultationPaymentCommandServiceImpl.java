package com.mindcluster.safediary.payments.application.internal.commandservices;

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

    public ConsultationPaymentCommandServiceImpl(ConnectedAccountRepository connectedAccountRepository,
                                                PaymentIntentRepository paymentIntentRepository,
                                                StripeGatewayClient stripeGatewayClient,
                                                StripeProperties stripeProperties) {
        this.connectedAccountRepository = connectedAccountRepository;
        this.paymentIntentRepository = paymentIntentRepository;
        this.stripeGatewayClient = stripeGatewayClient;
        this.stripeProperties = stripeProperties;
    }

    @Override
    @Transactional
    public Result<ConsultationCheckoutResultDto, ApplicationError> handle(CreateConsultationCheckoutCommand command) {
        try {
            Long psychologistAccountId = command.psychologistAccountId();
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

            BigDecimal platformFee = command.platformFee();
            if (platformFee == null || platformFee.compareTo(BigDecimal.ZERO) < 0) {
                double defaultPercent = stripeProperties.getMarketplace() != null
                        ? stripeProperties.getMarketplace().getFeePercent()
                        : 15.0;
                platformFee = command.totalAmount()
                        .multiply(BigDecimal.valueOf(defaultPercent / 100.0))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            String idempotencyKey = (command.idempotencyKey() != null && !command.idempotencyKey().isBlank())
                    ? command.idempotencyKey().trim()
                    : "care-appointment-" + command.appointmentId();

            // Record or retrieve PaymentIntent
            PaymentIntent paymentIntent = paymentIntentRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseGet(() -> {
                        PaymentIntent pi = new PaymentIntent(
                                "CONSULTATION",
                                command.appointmentId(),
                                command.patientAccountId(),
                                command.totalAmount(),
                                command.currency() != null ? command.currency() : "USD",
                                idempotencyKey
                        );
                        return paymentIntentRepository.save(pi);
                    });

            StripeCheckoutSession session = stripeGatewayClient.createConsultationCheckoutSession(
                    command.patientAccountId(),
                    psychologistAccountId,
                    connectedAccount.getStripeAccountId(),
                    command.totalAmount(),
                    platformFee,
                    command.currency(),
                    command.appointmentId(),
                    command.successUrl(),
                    command.cancelUrl(),
                    idempotencyKey
            );

            return Result.success(new ConsultationCheckoutResultDto(
                    command.appointmentId(),
                    command.patientAccountId(),
                    psychologistAccountId,
                    command.totalAmount(),
                    command.currency() != null ? command.currency() : "USD",
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
