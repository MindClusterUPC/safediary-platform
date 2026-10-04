package com.mindcluster.safediary.assistantai.domain.model.events;

import java.time.Instant;

/**
 * Raised when the user sends a message. It never carries the message content.
 */
public record UserMessageReceivedEvent(Long sessionId, Instant occurredAt) {
}
