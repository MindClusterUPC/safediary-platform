package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

import java.time.Instant;

/**
 * Item of the mobile chat history. The title is derived from the first user message.
 */
public record AssistantConversationSummaryResource(String conversationId, String title, String status,
                                                   Instant startedAt, Instant lastMessageAt) {
}
