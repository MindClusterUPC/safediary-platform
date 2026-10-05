package com.mindcluster.safediary.assistantai.application.queryservices;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ClinicalSummary;
import com.mindcluster.safediary.assistantai.domain.model.queries.GetWeeklyClinicalSummaryQuery;

import java.util.Optional;

/**
 * Application service contract for clinical summary queries.
 */
public interface ClinicalSummaryQueryService {

    Optional<ClinicalSummary> handle(GetWeeklyClinicalSummaryQuery query);
}
