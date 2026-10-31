package com.mindcluster.safediary.payments.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.payments.domain.model.aggregates.ConnectedAccount;
import com.mindcluster.safediary.payments.domain.repositories.ConnectedAccountRepository;
import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.assemblers.ConnectedAccountPersistenceAssembler;
import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.repositories.ConnectedAccountPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ConnectedAccountRepositoryImpl implements ConnectedAccountRepository {

    private final ConnectedAccountPersistenceRepository repository;

    public ConnectedAccountRepositoryImpl(ConnectedAccountPersistenceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ConnectedAccount> findById(Long id) {
        return repository.findById(id).map(ConnectedAccountPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<ConnectedAccount> findByPsychologistAccountId(Long psychologistAccountId) {
        return repository.findByPsychologistAccountId(psychologistAccountId)
                .map(ConnectedAccountPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<ConnectedAccount> findByStripeAccountId(String stripeAccountId) {
        return repository.findByStripeAccountId(stripeAccountId)
                .map(ConnectedAccountPersistenceAssembler::toDomain);
    }

    @Override
    public ConnectedAccount save(ConnectedAccount connectedAccount) {
        var entity = ConnectedAccountPersistenceAssembler.toEntity(connectedAccount);
        var saved = repository.save(entity);
        return ConnectedAccountPersistenceAssembler.toDomain(saved);
    }
}
