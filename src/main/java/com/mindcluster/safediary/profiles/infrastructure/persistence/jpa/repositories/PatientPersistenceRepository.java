package com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.entities.PatientPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientPersistenceRepository extends JpaRepository<PatientPersistenceEntity, Long> {
    Optional<PatientPersistenceEntity> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
