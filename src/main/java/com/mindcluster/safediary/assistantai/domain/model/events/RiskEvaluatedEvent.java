package com.mindcluster.safediary.assistantai.domain.model.events;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.RiskLevel;
import java.time.Instant;

/**
 * Raised every time the risk of a user message is evaluated.
 */
public record RiskEvaluatedEvent(Long sessionId, RiskLevel level, double score, Instant occurredAt) {
}
