package com.mindcluster.safediary.diary.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.diary.infrastructure.persistence.jpa.entities.DiaryEntryPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryEntryPersistenceRepository extends JpaRepository<DiaryEntryPersistenceEntity, Long> {
    List<DiaryEntryPersistenceEntity> findAllByPatientIdOrderByEntryDateDesc(Long patientId);
}
