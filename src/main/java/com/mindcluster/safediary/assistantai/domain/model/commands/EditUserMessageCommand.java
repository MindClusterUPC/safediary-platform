package com.mindcluster.safediary.assistantai.domain.model.commands;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;

/**
 * Command to edit an existing user message and regenerate subsequent responses.
 */
public record EditUserMessageCommand(
        Long accountId,
        Long conversationId,
        Long messageId,
        String prompt,
        String locale,
        PersonalityTone tone
) {
}
