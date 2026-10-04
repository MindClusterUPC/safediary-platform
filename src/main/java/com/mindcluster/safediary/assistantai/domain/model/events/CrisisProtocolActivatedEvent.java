package com.mindcluster.safediary.assistantai.domain.model.events;

import java.time.Instant;

/**
 * Raised when a session enters the crisis protocol.
 */
public record CrisisProtocolActivatedEvent(Long sessionId, Long accountId, Instant occurredAt) {
}
