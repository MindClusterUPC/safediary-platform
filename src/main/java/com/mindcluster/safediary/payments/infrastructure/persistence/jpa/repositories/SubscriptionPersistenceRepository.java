package com.mindcluster.safediary.payments.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.entities.SubscriptionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionPersistenceRepository extends JpaRepository<SubscriptionPersistenceEntity, Long> {
    Optional<SubscriptionPersistenceEntity> findByPatientAccountId(Long patientAccountId);
    boolean existsByPatientAccountId(Long patientAccountId);
}
