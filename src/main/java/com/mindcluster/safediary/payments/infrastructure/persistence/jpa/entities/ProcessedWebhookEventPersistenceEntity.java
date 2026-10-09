package com.mindcluster.safediary.payments.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "processed_webhook_events", schema = "payments", uniqueConstraints = {
        @UniqueConstraint(name = "ux_processed_events_event_id", columnNames = {"event_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class ProcessedWebhookEventPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "event_id", nullable = false, length = 255)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;
}
