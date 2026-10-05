package com.mindcluster.safediary.assistantai.domain;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.RiskAssessment;
import com.mindcluster.safediary.assistantai.domain.model.events.RiskEvaluatedEvent;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.RiskEvaluation;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.RiskLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskAssessmentTest {

    @Test
    void highRiskActivatesCrisisProtocolAndRaisesEvent() {
        var assessment = new RiskAssessment(1L, new RiskEvaluation(RiskLevel.HIGH, 0.7, List.of()));

        assertThat(assessment.isCrisisProtocolActivated()).isTrue();
        assertThat(assessment.domainEvents()).hasAtLeastOneElementOfType(RiskEvaluatedEvent.class);
    }

    @Test
    void lowRiskDoesNotActivateCrisisProtocol() {
        var assessment = new RiskAssessment(1L, new RiskEvaluation(RiskLevel.LOW, 0.0, List.of()));

        assertThat(assessment.isCrisisProtocolActivated()).isFalse();
    }

    @Test
    void requiresSessionId() {
        assertThatThrownBy(() -> new RiskAssessment(null, new RiskEvaluation(RiskLevel.LOW, 0.0, List.of())))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
