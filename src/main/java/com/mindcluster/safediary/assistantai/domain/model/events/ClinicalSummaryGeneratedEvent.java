package com.mindcluster.safediary.assistantai.domain.model.events;

import java.time.LocalDate;
import java.time.Instant;

/**
 * Raised when a weekly clinical summary is generated for an account.
 */
public record ClinicalSummaryGeneratedEvent(Long accountId, LocalDate periodStart, LocalDate periodEnd, Instant occurredAt) {
}
