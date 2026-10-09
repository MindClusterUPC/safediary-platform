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
@Table(name="clinician_profiles", schema="clinician_directory")
@Getter @Setter @NoArgsConstructor
public class ClinicianProfilePersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable=false, unique=true)
    private Long accountId;
    @Column(nullable=false, length=160)
    private String displayName;
    @Column(nullable=false, length=120)
    private String professionalTitle;
    @Column(nullable=false, length=2000)
    private String bio;
    @Column(length=512)
    private String bannerRef;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private VerificationStatus verificationStatus;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private PublicationStatus publicationStatus;

    @ElementCollection
    @CollectionTable(name="clinician_specialties", schema="clinician_directory", joinColumns=@JoinColumn(name="clinician_profile_id"))
    @Column(name="specialty_name", nullable=false, length=100)
    private List<String> specialties = new ArrayList<>();
}
