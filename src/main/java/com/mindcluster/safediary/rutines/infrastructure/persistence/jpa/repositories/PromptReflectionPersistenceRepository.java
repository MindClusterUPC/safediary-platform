package com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.rutines.domain.model.aggregates.PromptReflection.ReflectionStatus;
import com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.entities.PromptReflectionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromptReflectionPersistenceRepository extends JpaRepository<PromptReflectionPersistenceEntity, Long> {
    
    Optional<PromptReflectionPersistenceEntity> findByPatientIdAndStatus(Long patientId, ReflectionStatus status);
}