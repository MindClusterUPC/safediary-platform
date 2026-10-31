package com.mindcluster.safediary.payments.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.entities.PaymentIntentPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentIntentPersistenceRepository extends JpaRepository<PaymentIntentPersistenceEntity, Long> {
    Optional<PaymentIntentPersistenceEntity> findByIdempotencyKey(String idempotencyKey);
    Optional<PaymentIntentPersistenceEntity> findByProviderReference(String providerReference);
}
