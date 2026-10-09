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
@Table(name="appointments", schema="care_scheduling",
        indexes={@Index(name="ix_appointments_clinician_time", columnList="clinician_profile_id, startsAt"),
                 @Index(name="ix_appointments_patient", columnList="patientAccountId, startsAt")})
@Getter @Setter @NoArgsConstructor
public class AppointmentPersistenceEntity extends AuditableAbstractPersistenceEntity {
    private Long contactRequestId;
    @Column(nullable=false)
    private Long patientAccountId;
    @Column(name="clinician_profile_id", nullable=false)
    private Long clinicianId;
    @Column(nullable=false)
    private Instant startsAt;
    @Column(nullable=false)
    private Instant endsAt;
    @Column(nullable=false, length=40)
    private String timezone;
    @Column(nullable=false, precision=10, scale=2)
    private BigDecimal agreedAmount;
    @Column(nullable=false, length=3)
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private AppointmentStatus status;
    @Column(length=128)
    private String paymentReference;
}
