package com.mindcluster.safediary.assistantai.domain.model.events;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;
import java.time.Instant;

/**
 * Raised when the user changes the personality tone of the AI companion.
 */
public record PersonalityToneUpdatedEvent(Long sessionId, PersonalityTone tone, Instant occurredAt) {
}
