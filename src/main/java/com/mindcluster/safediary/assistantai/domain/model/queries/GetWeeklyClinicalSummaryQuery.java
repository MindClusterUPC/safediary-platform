package com.mindcluster.safediary.assistantai.domain.model.queries;

import java.time.LocalDate;

/**
 * Gets the clinical summary of a week. A null weekStart means the latest one.
 */
public record GetWeeklyClinicalSummaryQuery(Long accountId, LocalDate weekStart) {
}
