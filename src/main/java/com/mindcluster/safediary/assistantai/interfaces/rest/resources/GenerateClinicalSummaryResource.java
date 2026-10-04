package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Request to generate a weekly clinical summary. A missing weekStart means the last complete week.
 */
public record GenerateClinicalSummaryResource(@NotNull @Positive Long accountId, LocalDate weekStart, String locale) {
}
