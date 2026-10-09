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
@Table(name="summary_access_audits", schema="care_scheduling")
@Getter @Setter @NoArgsConstructor
public class SummaryAccessAuditPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable=false)
    private Long appointmentId;
    @Column(nullable=false)
    private Long clinicianAccountId;
    @Column(nullable=false, length=128)
    private String consentRef;
    @Column(nullable=false)
    private Instant accessedAt;
}
