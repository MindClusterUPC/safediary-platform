package com.mindcluster.safediary.assistantai.domain.model.events;

import java.time.Instant;

/**
 * Raised when the AI companion answers with a reflection.
 */
public record ReflectionGeneratedEvent(Long sessionId, Instant occurredAt) {
}
