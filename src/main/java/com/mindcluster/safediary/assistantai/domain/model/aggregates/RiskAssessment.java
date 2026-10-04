package com.mindcluster.safediary.assistantai.domain.model.aggregates;

import com.mindcluster.safediary.assistantai.domain.model.events.RiskEvaluatedEvent;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.RiskEvaluation;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.RiskLevel;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Immutable, append-only record of a risk evaluation performed on a conversation session.
 */
@Getter
public class RiskAssessment extends AbstractDomainAggregateRoot<RiskAssessment> {

    private Long id;
    private Long sessionId;
    private double riskScore;
    private RiskLevel riskLevel;
    private List<String> triggerKeywords;
    private boolean crisisProtocolActivated;
    private Instant assessedAt;

    /**
     * Records a new evaluation and raises {@link RiskEvaluatedEvent}.
     */
    public RiskAssessment(Long sessionId, RiskEvaluation evaluation) {
        if (sessionId == null) throw new IllegalArgumentException("sessionId must not be null");
        this.sessionId = sessionId;
        this.riskLevel = evaluation.level();
        this.riskScore = evaluation.score();
        this.triggerKeywords = evaluation.triggerKeywords();
        this.crisisProtocolActivated = riskLevel == RiskLevel.HIGH || riskLevel == RiskLevel.CRITICAL;
        this.assessedAt = Instant.now();
        registerDomainEvent(new RiskEvaluatedEvent(sessionId, riskLevel, riskScore, assessedAt));
    }

    /**
     * Rebuilds a persisted assessment. Only used by persistence assemblers.
     */
    public RiskAssessment(Long id, Long sessionId, double riskScore, RiskLevel riskLevel, List<String> triggerKeywords,
                          boolean crisisProtocolActivated, Instant assessedAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.triggerKeywords = List.copyOf(triggerKeywords);
        this.crisisProtocolActivated = crisisProtocolActivated;
        this.assessedAt = assessedAt;
    }
}
