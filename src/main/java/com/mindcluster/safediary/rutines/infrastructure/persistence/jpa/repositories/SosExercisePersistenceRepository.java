package com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.entities.SosExercisePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SosExercisePersistenceRepository extends JpaRepository<SosExercisePersistenceEntity, Long> {

    List<SosExercisePersistenceEntity> findAllByPatientId(Long patientId);
}