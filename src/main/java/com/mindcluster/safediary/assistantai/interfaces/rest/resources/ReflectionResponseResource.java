package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

import java.util.List;

/**
 * Answer of the AI companion to a user message.
 */
public record ReflectionResponseResource(Long sessionId, String sessionStatus, ConversationMessageResource userMessage, ConversationMessageResource assistantMessage, String riskLevel, List<CrisisHotlineResource> crisisResources) {
}
