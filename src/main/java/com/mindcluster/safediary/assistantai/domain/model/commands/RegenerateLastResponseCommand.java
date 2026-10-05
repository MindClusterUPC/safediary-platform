package com.mindcluster.safediary.assistantai.domain.model.commands;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;

/**
 * Command to regenerate the last assistant reply in a conversation session.
 */
public record RegenerateLastResponseCommand(
        Long accountId,
        Long conversationId,
        String locale,
        PersonalityTone tone
) {
}
