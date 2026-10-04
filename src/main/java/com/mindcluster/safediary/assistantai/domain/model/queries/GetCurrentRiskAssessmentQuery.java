package com.mindcluster.safediary.assistantai.domain.model.queries;

/**
 * Gets the latest risk assessment of a session.
 */
public record GetCurrentRiskAssessmentQuery(Long sessionId) {
}
