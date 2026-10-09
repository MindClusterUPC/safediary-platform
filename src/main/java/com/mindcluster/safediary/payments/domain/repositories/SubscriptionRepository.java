package com.mindcluster.safediary.payments.domain.repositories;

import com.mindcluster.safediary.payments.domain.model.aggregates.Subscription;

import java.util.Optional;

/**
 * Domain repository contract for managing Subscription aggregate persistence.
 */
public interface SubscriptionRepository {
    Subscription save(Subscription subscription);
    Optional<Subscription> findById(Long id);
    Optional<Subscription> findByPatientAccountId(Long patientAccountId);
    boolean existsByPatientAccountId(Long patientAccountId);
}
