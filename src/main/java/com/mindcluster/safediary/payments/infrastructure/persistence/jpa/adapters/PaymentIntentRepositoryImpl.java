package com.mindcluster.safediary.payments.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.payments.domain.model.aggregates.PaymentIntent;
import com.mindcluster.safediary.payments.domain.repositories.PaymentIntentRepository;
import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.assemblers.PaymentIntentPersistenceAssembler;
import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.repositories.PaymentIntentPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PaymentIntentRepositoryImpl implements PaymentIntentRepository {

    private final PaymentIntentPersistenceRepository repository;

    public PaymentIntentRepositoryImpl(PaymentIntentPersistenceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PaymentIntent> findById(Long id) {
        return repository.findById(id).map(PaymentIntentPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<PaymentIntent> findByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).map(PaymentIntentPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<PaymentIntent> findByProviderReference(String providerReference) {
        return repository.findByProviderReference(providerReference).map(PaymentIntentPersistenceAssembler::toDomain);
    }

    @Override
    public PaymentIntent save(PaymentIntent paymentIntent) {
        var entity = PaymentIntentPersistenceAssembler.toEntity(paymentIntent);
        var saved = repository.save(entity);
        return PaymentIntentPersistenceAssembler.toDomain(saved);
    }
}
