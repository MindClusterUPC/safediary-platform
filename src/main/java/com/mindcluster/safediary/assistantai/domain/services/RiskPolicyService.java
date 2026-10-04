package com.mindcluster.safediary.assistantai.domain.services;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.RiskEvaluation;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.RiskLevel;

import java.util.List;

/**
 * Decides the self-harm risk level of a user message and whether it requires the crisis protocol.
 * <p>
 * Combines a deterministic keyword policy with the score inferred by the language model,
 * so a crisis is never missed when the model is unavailable or underestimates the risk.
 * </p>
 */
public class RiskPolicyService {

    private static final List<String> CRITICAL_KEYWORDS = List.of(
            "suicid", "matarme", "quitarme la vida", "no quiero vivir", "quiero morir", "acabar con todo",
            "hacerme dano", "autolesion", "cortarme", "kill myself", "end my life", "want to die", "self harm");

    private static final List<String> MODERATE_KEYWORDS = List.of(
            "desesper", "sin salida", "no aguanto", "no puedo mas", "hopeless", "cant go on");

    public RiskEvaluation evaluate(String content, Double modelRiskScore) {
        var normalized = TextNormalizer.normalize(content);
        var found = CRITICAL_KEYWORDS.stream().filter(normalized::contains).toList();
        double keywordScore = 1.0;
        if (found.isEmpty()) {
            found = MODERATE_KEYWORDS.stream().filter(normalized::contains).toList();
            keywordScore = found.isEmpty() ? 0.0 : 0.5;
        }
        double modelScore = modelRiskScore == null ? 0.0 : Math.max(0.0, Math.min(1.0, modelRiskScore));
        double score = Math.max(keywordScore, modelScore);
        return new RiskEvaluation(levelFromScore(score), score, found);
    }

    public RiskLevel levelFromScore(double score) {
        if (score >= 0.85) return RiskLevel.CRITICAL;
        if (score >= 0.65) return RiskLevel.HIGH;
        if (score >= 0.4) return RiskLevel.MODERATE;
        return RiskLevel.LOW;
    }

    /**
     * Event Storming policy: "if the risk is high or critical, activate the crisis protocol".
     */
    public boolean requiresCrisisProtocol(RiskEvaluation evaluation) {
        return evaluation.level() == RiskLevel.HIGH || evaluation.level() == RiskLevel.CRITICAL;
    }
}
