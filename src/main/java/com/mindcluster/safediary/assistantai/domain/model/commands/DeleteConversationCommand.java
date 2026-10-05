package com.mindcluster.safediary.assistantai.domain.model.commands;

/**
 * Command to delete a conversation session and its related entities.
 */
public record DeleteConversationCommand(Long accountId, Long conversationId) {
}
