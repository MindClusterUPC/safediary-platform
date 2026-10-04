package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

/**
 * Risk assessment of a conversation session.
 */
public record RiskAssessmentResource(Long id, Long sessionId, double riskScore, String riskLevel,
                                     List<String> triggerKeywords, boolean crisisProtocolActivated,
                                     Instant assessedAt) {
}
