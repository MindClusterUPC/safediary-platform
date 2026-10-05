package com.mindcluster.safediary.assistantai.domain.model.valueobjects;

import java.util.List;

/**
 * Result of evaluating the self-harm risk of a user message.
 *
 * @param level           resulting risk level
 * @param score           risk score between 0.0 and 1.0
 * @param triggerKeywords keywords found in the message that raised the risk
 */
public record RiskEvaluation(RiskLevel level, double score, List<String> triggerKeywords) {
    public RiskEvaluation {
        triggerKeywords = List.copyOf(triggerKeywords);
    }
}
