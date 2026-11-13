package com.mindcluster.safediary.payments.application.internal.commandservices;

import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import com.mindcluster.safediary.payments.application.commandservices.PatientPaymentMethodCommandService;
import com.mindcluster.safediary.payments.application.commandservices.dto.PaymentMethodSetupResultDto;
import com.mindcluster.safediary.payments.application.outbound.StripeGatewayClient;
import com.mindcluster.safediary.payments.application.outbound.dto.StripeCheckoutSession;
import com.mindcluster.safediary.payments.application.queryservices.PatientPaymentsQueryService;
import com.mindcluster.safediary.payments.domain.model.aggregates.Subscription;
import com.mindcluster.safediary.payments.domain.model.commands.CreatePaymentMethodSetupCommand;
import com.mindcluster.safediary.payments.domain.model.commands.SetDefaultPaymentMethodCommand;
import com.mindcluster.safediary.payments.domain.repositories.SubscriptionRepository;
import com.mindcluster.safediary.payments.infrastructure.configuration.StripeProperties;
import com.mindcluster.safediary.payments.interfaces.rest.resources.PaymentMethodResource;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PatientPaymentMethodCommandServiceImpl implements PatientPaymentMethodCommandService {

    private static final Logger log = LoggerFactory.getLogger(PatientPaymentMethodCommandServiceImpl.class);

    private final SubscriptionRepository subscriptionRepository;
    private final StripeGatewayClient stripeGatewayClient;
    private final PatientPaymentsQueryService patientPaymentsQueryService;
    private final StripeProperties stripeProperties;
    private final IamContextFacade iamContextFacade;

    public PatientPaymentMethodCommandServiceImpl(SubscriptionRepository subscriptionRepository,
                                                  StripeGatewayClient stripeGatewayClient,
                                                  PatientPaymentsQueryService patientPaymentsQueryService,
                                                  @Autowired(required = false) StripeProperties stripeProperties,
                                                  @Autowired(required = false) IamContextFacade iamContextFacade) {
        this.subscriptionRepository = subscriptionRepository;
        this.stripeGatewayClient = stripeGatewayClient;
        this.patientPaymentsQueryService = patientPaymentsQueryService;
        this.stripeProperties = stripeProperties;
        this.iamContextFacade = iamContextFacade;
    }

    @Override
    @Transactional
    public Result<PaymentMethodSetupResultDto, ApplicationError> handle(CreatePaymentMethodSetupCommand command) {
        try {
            Long patientAccountId = command.patientAccountId();
            Subscription subscription = subscriptionRepository.findByPatientAccountId(patientAccountId)
                    .orElseGet(() -> subscriptionRepository.save(Subscription.createFree(patientAccountId)));

            String customerId = subscription.getStripeCustomerId();
            boolean hasApiKey = stripeProperties != null
                    && stripeProperties.getApiKey() != null
                    && !stripeProperties.getApiKey().isBlank();
            boolean needsCustomer = customerId == null
                    || customerId.isBlank()
                    || (hasApiKey && customerId.startsWith("cus_simulated_"));

            if (needsCustomer) {
                String email = null;
                if (iamContextFacade != null) {
                    email = iamContextFacade.fetchAccount(patientAccountId)
                            .map(acc -> acc.email())
                            .orElse(null);
                }
                customerId = stripeGatewayClient.createCustomer(patientAccountId, email);
                subscription.assignStripeCustomerId(customerId);
                subscriptionRepository.save(subscription);
            }

            String idempotencyKey = "pm-setup-" + patientAccountId + "-" + UUID.randomUUID();
            StripeCheckoutSession session = stripeGatewayClient.createPaymentMethodSetupSession(
                    patientAccountId,
                    customerId,
                    command.successUrl(),
                    command.cancelUrl(),
                    idempotencyKey
            );

            return Result.success(new PaymentMethodSetupResultDto(
                    patientAccountId,
                    session.checkoutUrl(),
                    session.sessionId(),
                    "Payment method setup session created successfully."
            ));
        } catch (IllegalArgumentException ex) {
            log.warn("Validation error creating payment method setup session: {}", ex.getMessage());
            return Result.failure(ApplicationError.validationError("payment-method-setup", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error creating payment method setup session", ex);
            return Result.failure(ApplicationError.unexpected("payment-method-setup", ex.getMessage()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<PaymentMethodResource>, ApplicationError> handle(SetDefaultPaymentMethodCommand command) {
        try {
            Long patientAccountId = command.patientAccountId();
            var subOpt = subscriptionRepository.findByPatientAccountId(patientAccountId);
            if (subOpt.isPresent() && subOpt.get().getStripeCustomerId() != null
                    && !subOpt.get().getStripeCustomerId().isBlank()) {
                stripeGatewayClient.setDefaultCustomerPaymentMethod(
                        subOpt.get().getStripeCustomerId(),
                        command.paymentMethodId()
                );
            }
            List<PaymentMethodResource> methods = patientPaymentsQueryService.getSavedPaymentMethods(patientAccountId);
            return Result.success(methods);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("payment-method-default", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error setting default payment method", ex);
            return Result.failure(ApplicationError.unexpected("payment-method-default", ex.getMessage()));
        }
    }
}
