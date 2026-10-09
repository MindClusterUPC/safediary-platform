package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.ConsultationRatePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ConsultationRatePersistenceRepository extends JpaRepository<ConsultationRatePersistenceEntity, Long> {
    Optional<ConsultationRatePersistenceEntity> findFirstByClinicianIdOrderByVersionDesc(Long id);
    List<ConsultationRatePersistenceEntity> findByClinicianIdAndActiveTrue(Long id);
}
