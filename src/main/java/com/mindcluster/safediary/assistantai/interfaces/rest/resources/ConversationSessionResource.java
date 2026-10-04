package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

/**
 * Conversation session with its messages.
 */
public record ConversationSessionResource(Long id, Long accountId, String status, String currentTone, Instant startedAt, Instant endedAt, List<ConversationMessageResource> messages) {
}
