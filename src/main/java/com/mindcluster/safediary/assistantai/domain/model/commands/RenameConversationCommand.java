package com.mindcluster.safediary.assistantai.domain.model.commands;

/**
 * Command to rename a conversation session.
 */
public record RenameConversationCommand(Long accountId, Long conversationId, String title) {
}
