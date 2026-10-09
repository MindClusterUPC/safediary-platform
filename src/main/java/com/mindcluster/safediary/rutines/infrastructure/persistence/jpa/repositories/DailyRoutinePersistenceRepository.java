package com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.rutines.infrastructure.persistence.jpa.entities.DailyRoutinePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyRoutinePersistenceRepository extends JpaRepository<DailyRoutinePersistenceEntity, Long> {
    
    List<DailyRoutinePersistenceEntity> findAllByPatientId(Long patientId);
}