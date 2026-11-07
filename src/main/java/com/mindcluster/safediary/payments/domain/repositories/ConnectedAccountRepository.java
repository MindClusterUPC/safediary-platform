package com.mindcluster.safediary.payments.domain.repositories;

import com.mindcluster.safediary.payments.domain.model.aggregates.ConnectedAccount;

import java.util.Optional;

public interface ConnectedAccountRepository {
    Optional<ConnectedAccount> findById(Long id);
    Optional<ConnectedAccount> findByPsychologistAccountId(Long psychologistAccountId);
    Optional<ConnectedAccount> findByStripeAccountId(String stripeAccountId);
    ConnectedAccount save(ConnectedAccount connectedAccount);
}
