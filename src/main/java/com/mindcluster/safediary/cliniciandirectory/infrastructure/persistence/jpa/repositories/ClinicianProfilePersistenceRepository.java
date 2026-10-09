package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.ClinicianProfilePersistenceEntity;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface ClinicianProfilePersistenceRepository extends JpaRepository<ClinicianProfilePersistenceEntity, Long> {
    Optional<ClinicianProfilePersistenceEntity> findByAccountId(Long accountId);
    List<ClinicianProfilePersistenceEntity> findByVerificationStatusAndPublicationStatus(VerificationStatus verification, PublicationStatus publication);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ClinicianProfilePersistenceEntity p where p.id = :id")
    Optional<ClinicianProfilePersistenceEntity> findByIdForUpdate(@Param("id") Long id);
}
