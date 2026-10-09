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
@Table(name="trust_scores", schema="clinician_directory")
@Getter @Setter @NoArgsConstructor
public class TrustScorePersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(name="clinician_profile_id", nullable=false, unique=true)
    private Long clinicianId;
    @Column(precision=5, scale=2)
    private BigDecimal score;
    @Column(nullable=false)
    private boolean sufficientData;
    @Column(precision=5, scale=2)
    private BigDecimal ratingFactor;
    @Column(nullable=false)
    private long completedSessions;
    @Column(nullable=false)
    private long completedMinutes;
    @Column(nullable=false)
    private int reviewCount;

}
