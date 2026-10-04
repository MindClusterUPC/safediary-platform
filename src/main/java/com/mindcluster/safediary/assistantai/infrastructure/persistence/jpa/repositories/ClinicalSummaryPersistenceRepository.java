package com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.entities.ClinicalSummaryPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Spring Data repository for clinical summary persistence entities.
 */
@Repository
public interface ClinicalSummaryPersistenceRepository extends JpaRepository<ClinicalSummaryPersistenceEntity, Long> {

    Optional<ClinicalSummaryPersistenceEntity> findFirstByAccountIdAndPeriodStart(Long accountId, LocalDate periodStart);

    Optional<ClinicalSummaryPersistenceEntity> findFirstByAccountIdOrderByPeriodStartDesc(Long accountId);
}
