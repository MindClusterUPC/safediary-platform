package com.mindcluster.safediary.assistantai.application.commandservices;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ClinicalSummary;
import com.mindcluster.safediary.assistantai.domain.model.commands.GenerateWeeklyClinicalSummaryCommand;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;

/**
 * Application service contract for clinical summary commands.
 */
public interface ClinicalSummaryCommandService {

    /**
     * Generates the weekly summary, or returns the existing one for that week (idempotent).
     */
    Result<ClinicalSummary, ApplicationError> handle(GenerateWeeklyClinicalSummaryCommand command);
}
