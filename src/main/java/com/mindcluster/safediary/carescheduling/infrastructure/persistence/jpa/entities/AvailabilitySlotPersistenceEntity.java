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

/** Explicit entity name: the legacy Profiles context has an entity with the same class name. */
@Entity(name="CareAvailabilitySlotPersistenceEntity")
@Table(name="availability_slots", schema="care_scheduling",
        indexes=@Index(name="ix_availability_slots_clinician", columnList="clinician_profile_id"))
@Getter @Setter @NoArgsConstructor
public class AvailabilitySlotPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(name="clinician_profile_id", nullable=false)
    private Long clinicianId;
    /** ISO day of week: 1 = Monday. */
    @Column(nullable=false)
    private short dayOfWeek;
    @Column(nullable=false)
    private LocalTime startTime;
    @Column(nullable=false)
    private LocalTime endTime;
    @Column(name="is_active", nullable=false)
    private boolean active;
}
