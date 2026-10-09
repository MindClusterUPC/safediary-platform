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

public interface ContactRequestPersistenceRepository extends JpaRepository<ContactRequestPersistenceEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ContactRequestPersistenceEntity r where r.id = :id")
    Optional<ContactRequestPersistenceEntity> findByIdForUpdate(@Param("id") Long id);
    List<ContactRequestPersistenceEntity> findByPatientAccountIdAndClinicianIdAndStatusInOrderByIdDesc(
            Long patientAccountId, Long clinicianId, Collection<ContactRequestStatus> statuses);
    List<ContactRequestPersistenceEntity> findByPatientAccountId(Long patientAccountId);
    List<ContactRequestPersistenceEntity> findByClinicianId(Long clinicianId);
}
