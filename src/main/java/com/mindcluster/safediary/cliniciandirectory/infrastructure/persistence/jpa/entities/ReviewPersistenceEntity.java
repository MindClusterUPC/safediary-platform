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
@Table(name="reviews", schema="clinician_directory")
@Getter @Setter @NoArgsConstructor
public class ReviewPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(name="clinician_profile_id", nullable=false)
    private Long clinicianId;
    @Column(nullable=false, unique=true)
    private Long appointmentId;
    @Column(nullable=false)
    private Long patientAccountId;
    @Column()
    private Integer rating;
    @Column(name="review_text", length=2000)
    private String text;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private ReviewStatus status;
    @Column(nullable=false)
    private Instant publishedAt;

}
