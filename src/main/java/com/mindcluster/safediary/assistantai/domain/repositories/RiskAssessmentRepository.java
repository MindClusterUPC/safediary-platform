package com.mindcluster.safediary.assistantai.domain.repositories;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.RiskAssessment;

import java.util.Optional;

/**
 * Append-only risk assessment repository port.
 */
public interface RiskAssessmentRepository {

    RiskAssessment save(RiskAssessment assessment);

    Optional<RiskAssessment> findLatestBySessionId(Long sessionId);
}
