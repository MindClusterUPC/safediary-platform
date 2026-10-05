package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Weekly clinical summary.
 */
public record ClinicalSummaryResource(Long id, Long accountId, LocalDate periodStart, LocalDate periodEnd,
                                      List<String> dominantEmotions, List<String> keyTriggers,
                                      String synthesisNarrative, List<String> highlights, Instant generatedAt) {
}
