package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

import java.time.Instant;

/**
 * Message of a mobile chat conversation. Role is "user" or "assistant".
 */
public record AssistantChatMessageResource(Long id, String role, String content, Instant sentAt) {
}
