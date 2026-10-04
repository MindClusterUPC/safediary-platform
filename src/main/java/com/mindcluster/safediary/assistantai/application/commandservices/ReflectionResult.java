package com.mindcluster.safediary.assistantai.application.commandservices;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;
import com.mindcluster.safediary.assistantai.domain.model.entities.ConversationMessage;

/**
 * Outcome of sending a message to the AI companion.
 */
public record ReflectionResult(ConversationSession session, ConversationMessage userMessage,
                               ConversationMessage assistantMessage) {
}
