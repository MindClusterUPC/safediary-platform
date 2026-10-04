package com.mindcluster.safediary.assistantai.domain.repositories;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ClinicalSummary;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Clinical summary repository port.
 */
public interface ClinicalSummaryRepository {

    ClinicalSummary save(ClinicalSummary summary);

    Optional<ClinicalSummary> findByAccountIdAndPeriodStart(Long accountId, LocalDate periodStart);

    Optional<ClinicalSummary> findLatestByAccountId(Long accountId);
}
