package com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.entities.RiskAssessmentPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Spring Data repository for risk assessment persistence entities.
 */
@Repository
public interface RiskAssessmentPersistenceRepository extends JpaRepository<RiskAssessmentPersistenceEntity, Long> {

    Optional<RiskAssessmentPersistenceEntity> findFirstBySessionIdOrderByAssessedAtDesc(Long sessionId);

    @Modifying
    @Query("DELETE FROM RiskAssessmentPersistenceEntity r WHERE r.sessionId = :sessionId")
    void deleteAllBySessionId(@Param("sessionId") Long sessionId);
}
