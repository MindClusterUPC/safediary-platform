package com.mindcluster.safediary.assistantai.domain.model.commands;

/**
 * Sends a prompt through the mobile-compatible chat contract, resolving the session automatically.
 */
public record SendChatPromptCommand(Long accountId, String conversationId, String prompt, String locale) {
}
