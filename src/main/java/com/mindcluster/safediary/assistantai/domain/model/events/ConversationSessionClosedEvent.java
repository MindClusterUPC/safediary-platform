package com.mindcluster.safediary.assistantai.domain.model.events;

import java.time.Instant;

/**
 * Raised when a conversation session is closed.
 */
public record ConversationSessionClosedEvent(Long sessionId, Instant occurredAt) {
}
