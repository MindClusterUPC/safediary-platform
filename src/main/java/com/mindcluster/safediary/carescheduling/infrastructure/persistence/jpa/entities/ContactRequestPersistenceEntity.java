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
@Table(name="contact_requests", schema="care_scheduling",
        indexes={@Index(name="ix_contact_requests_patient", columnList="patientAccountId"),
                 @Index(name="ix_contact_requests_clinician", columnList="clinician_profile_id")})
@Getter @Setter @NoArgsConstructor
public class ContactRequestPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable=false)
    private Long patientAccountId;
    @Column(name="clinician_profile_id", nullable=false)
    private Long clinicianId;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private ContactRequestStatus status;
}
