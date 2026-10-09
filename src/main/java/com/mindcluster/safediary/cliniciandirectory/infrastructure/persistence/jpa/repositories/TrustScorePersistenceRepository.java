package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.TrustScorePersistenceEntity;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface TrustScorePersistenceRepository extends JpaRepository<TrustScorePersistenceEntity, Long> {
    Optional<TrustScorePersistenceEntity> findByClinicianId(Long id);
}
