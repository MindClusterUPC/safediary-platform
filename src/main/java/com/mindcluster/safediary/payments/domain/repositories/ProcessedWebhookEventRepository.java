package com.mindcluster.safediary.payments.domain.repositories;

import com.mindcluster.safediary.payments.domain.model.entities.ProcessedWebhookEvent;

import java.util.Optional;

/**
 * Domain repository contract for tracking processed webhook events for idempotency.
 */
public interface ProcessedWebhookEventRepository {
    ProcessedWebhookEvent save(ProcessedWebhookEvent event);
    boolean existsByEventId(String eventId);
    Optional<ProcessedWebhookEvent> findByEventId(String eventId);
}
