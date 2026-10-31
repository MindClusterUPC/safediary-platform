package com.mindcluster.safediary.payments.application.internal.queryservices;

import com.mindcluster.safediary.payments.application.queryservices.ConnectedAccountQueryService;
import com.mindcluster.safediary.payments.application.queryservices.dto.ConnectedAccountDto;
import com.mindcluster.safediary.payments.domain.model.aggregates.ConnectedAccount;
import com.mindcluster.safediary.payments.domain.model.queries.GetConnectedAccountQuery;
import com.mindcluster.safediary.payments.domain.repositories.ConnectedAccountRepository;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConnectedAccountQueryServiceImpl implements ConnectedAccountQueryService {

    private final ConnectedAccountRepository connectedAccountRepository;

    public ConnectedAccountQueryServiceImpl(ConnectedAccountRepository connectedAccountRepository) {
        this.connectedAccountRepository = connectedAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Result<ConnectedAccountDto, ApplicationError> handle(GetConnectedAccountQuery query) {
        var accountOpt = connectedAccountRepository.findByPsychologistAccountId(query.psychologistAccountId());
        if (accountOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("ConnectedAccount",
                    "No Stripe connected account found for psychologist " + query.psychologistAccountId()));
        }

        ConnectedAccount account = accountOpt.get();
        return Result.success(new ConnectedAccountDto(
                account.getPsychologistAccountId(),
                account.getStripeAccountId(),
                account.isChargesEnabled(),
                account.isPayoutsEnabled(),
                account.isDetailsSubmitted(),
                account.isReady()
        ));
    }
}
