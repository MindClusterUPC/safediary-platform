package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

/**
 * Answer of the AI companion to a user message.
 */
public record ReflectionResponseResource(Long sessionId, String sessionStatus, ConversationMessageResource userMessage, ConversationMessageResource assistantMessage) {
}
