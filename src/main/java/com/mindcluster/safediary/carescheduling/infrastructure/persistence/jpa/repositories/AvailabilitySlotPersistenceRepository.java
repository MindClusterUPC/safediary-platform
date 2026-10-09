package com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.entities.*;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AvailabilitySlotPersistenceRepository extends JpaRepository<AvailabilitySlotPersistenceEntity, Long> {
    List<AvailabilitySlotPersistenceEntity> findByClinicianIdAndActiveTrue(Long clinicianId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from CareAvailabilitySlotPersistenceEntity a where a.clinicianId = :clinicianId and a.active = true")
    List<AvailabilitySlotPersistenceEntity> lockActiveByClinicianId(@Param("clinicianId") Long clinicianId);
}
