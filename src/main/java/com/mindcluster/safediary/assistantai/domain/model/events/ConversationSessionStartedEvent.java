package com.mindcluster.safediary.assistantai.domain.model.events;

import java.time.Instant;

/**
 * Raised when a new conversation session is persisted.
 */
public record ConversationSessionStartedEvent(Long sessionId, Long accountId, Instant occurredAt) {
}
