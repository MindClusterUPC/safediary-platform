package com.mindcluster.safediary.payments.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.payments.domain.model.entities.ProcessedWebhookEvent;
import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.entities.ProcessedWebhookEventPersistenceEntity;

public final class ProcessedWebhookEventPersistenceAssembler {

    private ProcessedWebhookEventPersistenceAssembler() {}

    public static ProcessedWebhookEvent toDomain(ProcessedWebhookEventPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ProcessedWebhookEvent(
                entity.getId(),
                entity.getEventId(),
                entity.getEventType(),
                entity.getProcessedAt()
        );
    }

    public static ProcessedWebhookEventPersistenceEntity toEntity(ProcessedWebhookEvent domain) {
        if (domain == null) {
            return null;
        }
        ProcessedWebhookEventPersistenceEntity entity = new ProcessedWebhookEventPersistenceEntity();
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        entity.setEventId(domain.getEventId());
        entity.setEventType(domain.getEventType());
        entity.setProcessedAt(domain.getProcessedAt());
        return entity;
    }
}
