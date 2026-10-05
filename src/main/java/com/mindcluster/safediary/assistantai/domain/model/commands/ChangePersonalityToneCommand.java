package com.mindcluster.safediary.assistantai.domain.model.commands;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;

/**
 * Changes the personality tone of the AI companion in a session.
 */
public record ChangePersonalityToneCommand(Long sessionId, PersonalityTone tone) {
}
