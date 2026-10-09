package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.VerificationRequestPersistenceEntity;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface VerificationRequestPersistenceRepository extends JpaRepository<VerificationRequestPersistenceEntity, Long> {
    @Query("select e.clinicianId from VerificationRequestPersistenceEntity e where e.id = :id")
    Optional<Long> findClinicianIdById(@Param("id") Long id);
    Optional<VerificationRequestPersistenceEntity> findFirstByClinicianIdOrderBySubmittedAtDescIdDesc(Long id);
    List<VerificationRequestPersistenceEntity> findByStatusOrderBySubmittedAtAsc(VerificationStatus status);
}
