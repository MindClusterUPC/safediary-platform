package com.mindcluster.safediary.payments.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.entities.ProcessedWebhookEventPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessedWebhookEventPersistenceRepository extends JpaRepository<ProcessedWebhookEventPersistenceEntity, Long> {
    boolean existsByEventId(String eventId);
    Optional<ProcessedWebhookEventPersistenceEntity> findByEventId(String eventId);
}
