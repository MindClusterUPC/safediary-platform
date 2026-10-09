package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name="consultation_rates", schema="clinician_directory",
        uniqueConstraints=@UniqueConstraint(name="ux_rate_version", columnNames={"clinician_profile_id", "version"}))
@Getter @Setter
public class ConsultationRatePersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(name="clinician_profile_id", nullable=false) private Long clinicianId;
    @Column(nullable=false, precision=10, scale=2) private BigDecimal amount;
    @Column(nullable=false, length=3) private String currency;
    @Column(nullable=false) private int durationMinutes;
    @Column(nullable=false) private int version;
    @Column(nullable=false) private Instant effectiveAt;
    @Column(name="is_active", nullable=false) private boolean active;
}
