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
@Table(name="slot_holds", schema="care_scheduling",
        indexes=@Index(name="ix_slot_holds_expiration", columnList="status, expiresAt"))
@Getter @Setter @NoArgsConstructor
public class SlotHoldPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable=false, unique=true)
    private Long appointmentId;
    @Column(nullable=false)
    private Instant expiresAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private SlotHoldStatus status;
}
