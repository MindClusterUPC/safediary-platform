package com.mindcluster.safediary.payments.application.internal.commandservices;

import com.mindcluster.safediary.payments.application.commandservices.ConnectedAccountCommandService;
import com.mindcluster.safediary.payments.application.commandservices.dto.ConnectedAccountOnboardingDto;
import com.mindcluster.safediary.payments.application.outbound.StripeGatewayClient;
import com.mindcluster.safediary.payments.domain.model.aggregates.ConnectedAccount;
import com.mindcluster.safediary.payments.domain.model.commands.CreateConnectedAccountCommand;
import com.mindcluster.safediary.payments.domain.repositories.ConnectedAccountRepository;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConnectedAccountCommandServiceImpl implements ConnectedAccountCommandService {

    private static final Logger log = LoggerFactory.getLogger(ConnectedAccountCommandServiceImpl.class);

    private final ConnectedAccountRepository connectedAccountRepository;
    private final StripeGatewayClient stripeGatewayClient;

    public ConnectedAccountCommandServiceImpl(ConnectedAccountRepository connectedAccountRepository,
                                             StripeGatewayClient stripeGatewayClient) {
        this.connectedAccountRepository = connectedAccountRepository;
        this.stripeGatewayClient = stripeGatewayClient;
    }

    @Override
    @Transactional
    public Result<ConnectedAccountOnboardingDto, ApplicationError> handle(CreateConnectedAccountCommand command) {
        try {
            Long psychologistAccountId = command.psychologistAccountId();

            ConnectedAccount account = connectedAccountRepository.findByPsychologistAccountId(psychologistAccountId)
                    .orElseGet(() -> {
                        String stripeAccountId = stripeGatewayClient.createConnectedAccount(psychologistAccountId, command.email());
                        ConnectedAccount newAccount = new ConnectedAccount(psychologistAccountId, stripeAccountId);
                        return connectedAccountRepository.save(newAccount);
                    });

            String onboardingUrl = stripeGatewayClient.createAccountLink(
                    account.getStripeAccountId(),
                    command.refreshUrl(),
                    command.returnUrl()
            );

            return Result.success(new ConnectedAccountOnboardingDto(
                    account.getPsychologistAccountId(),
                    account.getStripeAccountId(),
                    onboardingUrl,
                    account.isChargesEnabled(),
                    account.isPayoutsEnabled(),
                    account.isDetailsSubmitted()
            ));
        } catch (IllegalArgumentException ex) {
            log.warn("Validation error initiating connected account onboarding: {}", ex.getMessage());
            return Result.failure(ApplicationError.validationError("connected-account", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error creating connected account onboarding", ex);
            return Result.failure(ApplicationError.unexpected("connected-account", ex.getMessage()));
        }
    }
}
