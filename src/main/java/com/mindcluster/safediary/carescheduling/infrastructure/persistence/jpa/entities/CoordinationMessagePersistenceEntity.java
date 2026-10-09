package com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;

@Entity
@Table(name="coordination_messages", schema="care_scheduling",
        indexes=@Index(name="ix_coordination_messages_request", columnList="contactRequestId, sentAt"))
@Getter @Setter @NoArgsConstructor
public class CoordinationMessagePersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable=false)
    private Long contactRequestId;
    @Column(nullable=false)
    private Long senderAccountId;
    @Column(name="body_text", nullable=false, length=500)
    private String body;
    @Column(nullable=false)
    private Instant sentAt;
}
