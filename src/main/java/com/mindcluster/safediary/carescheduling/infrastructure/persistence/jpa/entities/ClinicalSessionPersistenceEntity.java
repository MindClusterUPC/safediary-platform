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
@Table(name="clinical_sessions", schema="care_scheduling")
@Getter @Setter @NoArgsConstructor
public class ClinicalSessionPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable=false, unique=true)
    private Long appointmentId;
    @Column(length=255)
    private String providerRoomRef;
    private Instant startedAt;
    private Instant endedAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private ClinicalSessionStatus status;
}
