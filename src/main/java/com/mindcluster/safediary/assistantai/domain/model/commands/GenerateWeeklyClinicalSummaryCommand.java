package com.mindcluster.safediary.assistantai.domain.model.commands;

import java.time.LocalDate;

/**
 * Generates the clinical summary of a week. A null weekStart means the last complete week.
 */
public record GenerateWeeklyClinicalSummaryCommand(Long accountId, LocalDate weekStart, String locale) {
}
