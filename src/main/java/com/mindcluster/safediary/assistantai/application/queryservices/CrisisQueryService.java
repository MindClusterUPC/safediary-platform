package com.mindcluster.safediary.assistantai.application.queryservices;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.RiskAssessment;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetCurrentRiskAssessmentQuery;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.CrisisHotline;

import java.util.List;
import java.util.Optional;

/**
 * Application service contract for crisis and risk queries.
 */
public interface CrisisQueryService {

    Optional<RiskAssessment> handle(GetCurrentRiskAssessmentQuery query);

    List<CrisisHotline> getCrisisHotlines();
}
