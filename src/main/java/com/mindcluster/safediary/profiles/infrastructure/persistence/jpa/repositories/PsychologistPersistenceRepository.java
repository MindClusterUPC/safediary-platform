package com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.entities.PsychologistPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PsychologistPersistenceRepository extends JpaRepository<PsychologistPersistenceEntity, Long> {
    Optional<PsychologistPersistenceEntity> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
