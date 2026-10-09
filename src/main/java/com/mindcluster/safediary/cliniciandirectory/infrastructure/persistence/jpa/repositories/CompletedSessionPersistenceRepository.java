package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.CompletedSessionPersistenceEntity;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface CompletedSessionPersistenceRepository extends JpaRepository<CompletedSessionPersistenceEntity, Long> {
    Optional<CompletedSessionPersistenceEntity> findByAppointmentId(Long id);
    List<CompletedSessionPersistenceEntity> findByClinicianId(Long id);
}
