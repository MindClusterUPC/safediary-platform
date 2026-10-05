package com.mindcluster.safediary.assistantai.domain.model.commands;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;

/**
 * Sends a prompt through the mobile-compatible chat contract, resolving the session automatically.
 *
 * @param tone personality chosen in the app; {@code null} keeps the conversation's current one
 */
public record SendChatPromptCommand(Long accountId, String conversationId, String prompt, String locale,
                                    PersonalityTone tone) {
}
