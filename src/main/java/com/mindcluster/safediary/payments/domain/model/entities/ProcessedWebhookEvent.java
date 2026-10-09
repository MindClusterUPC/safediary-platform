package com.mindcluster.safediary.payments.domain.model.entities;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain entity recording a processed Stripe webhook event to guarantee strict idempotency.
 */
@Getter
public class ProcessedWebhookEvent {

    private Long id;
    private final String eventId;
    private final String eventType;
    private final Instant processedAt;

    public ProcessedWebhookEvent(String eventId, String eventType, Instant processedAt) {
        this.eventId = Objects.requireNonNull(eventId, "eventId cannot be null");
        this.eventType = Objects.requireNonNull(eventType, "eventType cannot be null");
        this.processedAt = Objects.requireNonNullElseGet(processedAt, Instant::now);
    }

    public ProcessedWebhookEvent(Long id, String eventId, String eventType, Instant processedAt) {
        this(eventId, eventType, processedAt);
        this.id = id;
    }
}
