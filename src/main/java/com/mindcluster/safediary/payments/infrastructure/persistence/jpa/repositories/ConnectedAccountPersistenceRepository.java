package com.mindcluster.safediary.payments.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.entities.ConnectedAccountPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConnectedAccountPersistenceRepository extends JpaRepository<ConnectedAccountPersistenceEntity, Long> {
    Optional<ConnectedAccountPersistenceEntity> findByPsychologistAccountId(Long psychologistAccountId);
    Optional<ConnectedAccountPersistenceEntity> findByStripeAccountId(String stripeAccountId);
}
