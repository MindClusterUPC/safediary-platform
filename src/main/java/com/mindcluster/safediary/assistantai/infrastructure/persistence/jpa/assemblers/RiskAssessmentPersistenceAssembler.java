package com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.RiskAssessment;
import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.entities.RiskAssessmentPersistenceEntity;

import java.util.List;

/**
 * Static assembler between risk assessment domain and persistence representations.
 */
public final class RiskAssessmentPersistenceAssembler {

    private RiskAssessmentPersistenceAssembler() {
    }

    public static RiskAssessment toDomainFromPersistence(RiskAssessmentPersistenceEntity entity) {
        var keywords = entity.getTriggerKeywords();
        return new RiskAssessment(entity.getId(), entity.getSessionId(), entity.getRiskScore(), entity.getRiskLevel(),
                keywords == null || keywords.isBlank() ? List.of() : List.of(keywords.split(",")),
                entity.isCrisisProtocolActivated(), entity.getAssessedAt());
    }

    public static RiskAssessmentPersistenceEntity toPersistenceFromDomain(RiskAssessment assessment) {
        var entity = new RiskAssessmentPersistenceEntity();
        if (assessment.getId() != null) entity.setId(assessment.getId());
        entity.setSessionId(assessment.getSessionId());
        entity.setRiskScore(assessment.getRiskScore());
        entity.setRiskLevel(assessment.getRiskLevel());
        entity.setTriggerKeywords(String.join(",", assessment.getTriggerKeywords()));
        entity.setCrisisProtocolActivated(assessment.isCrisisProtocolActivated());
        entity.setAssessedAt(assessment.getAssessedAt());
        return entity;
    }
}
