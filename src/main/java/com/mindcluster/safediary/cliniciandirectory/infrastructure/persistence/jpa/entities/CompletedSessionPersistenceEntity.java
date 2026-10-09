package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name="completed_sessions", schema="clinician_directory")
@Getter @Setter @NoArgsConstructor
public class CompletedSessionPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable=false, unique=true)
    private Long appointmentId;
    @Column(name="clinician_profile_id", nullable=false)
    private Long clinicianId;
    @Column(nullable=false)
    private Long patientAccountId;
    @Column(nullable=false)
    private int durationMinutes;
    @Column(nullable=false)
    private Instant completedAt;

}
