package com.mindcluster.safediary.assistantai.domain.model.commands;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;

/**
 * Starts a new conversation session for an account.
 */
public record StartConversationCommand(Long accountId, PersonalityTone tone) {
}
