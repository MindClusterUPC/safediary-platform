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
@Table(name="verification_requests", schema="clinician_directory")
@Getter @Setter @NoArgsConstructor
public class VerificationRequestPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(name="clinician_profile_id", nullable=false)
    private Long clinicianId;
    @Column(nullable=false, length=120)
    private String licenseNumber;
    @Column(nullable=false, length=100)
    private String specialty;
    @Column(nullable=false, length=512)
    private String documentRef;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private VerificationStatus status;
    @Column(nullable=false)
    private Instant submittedAt;
    @Column()
    private Long reviewedByAccountId;
    @Column()
    private Instant reviewedAt;
    @Column(length=500)
    private String rejectionReason;

}
