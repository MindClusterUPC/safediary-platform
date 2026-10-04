package com.mindcluster.safediary.assistantai.domain;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.RiskLevel;
import com.mindcluster.safediary.assistantai.domain.services.RiskPolicyService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RiskPolicyServiceTest {

    private final RiskPolicyService service = new RiskPolicyService();

    @Test
    void criticalKeywordIsCritical() {
        var evaluation = service.evaluate("Ya no quiero vivir", null);

        assertThat(evaluation.level()).isEqualTo(RiskLevel.CRITICAL);
        assertThat(evaluation.triggerKeywords()).contains("no quiero vivir");
        assertThat(service.requiresCrisisProtocol(evaluation)).isTrue();
    }

    @Test
    void moderateKeywordDoesNotRequireCrisis() {
        var evaluation = service.evaluate("Me siento desesperada", null);

        assertThat(evaluation.level()).isEqualTo(RiskLevel.MODERATE);
        assertThat(service.requiresCrisisProtocol(evaluation)).isFalse();
    }

    @Test
    void neutralTextIsLow() {
        assertThat(service.evaluate("Hoy fue un buen día", null).level()).isEqualTo(RiskLevel.LOW);
    }

    @Test
    void highModelScoreRequiresCrisis() {
        var evaluation = service.evaluate("Hoy fue un buen día", 0.7);

        assertThat(evaluation.level()).isEqualTo(RiskLevel.HIGH);
        assertThat(service.requiresCrisisProtocol(evaluation)).isTrue();
    }

    @Test
    void veryHighModelScoreIsCritical() {
        assertThat(service.evaluate("Hoy fue un buen día", 0.9).level()).isEqualTo(RiskLevel.CRITICAL);
    }

    @Test
    void accentsAreNormalized() {
        assertThat(service.evaluate("Quiero hacerme daño", null).level()).isEqualTo(RiskLevel.CRITICAL);
    }
}
