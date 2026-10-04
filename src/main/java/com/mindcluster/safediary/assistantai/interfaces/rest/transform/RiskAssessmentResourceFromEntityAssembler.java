package com.mindcluster.safediary.assistantai.interfaces.rest.transform;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.RiskAssessment;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.RiskAssessmentResource;

/**
 * Assembler from risk assessment aggregate to resource.
 */
public final class RiskAssessmentResourceFromEntityAssembler {

    private RiskAssessmentResourceFromEntityAssembler() {
    }

    public static RiskAssessmentResource toResource(RiskAssessment assessment) {
        return new RiskAssessmentResource(assessment.getId(), assessment.getSessionId(), assessment.getRiskScore(),
                assessment.getRiskLevel().name(), assessment.getTriggerKeywords(),
                assessment.isCrisisProtocolActivated(), assessment.getAssessedAt());
    }
}
