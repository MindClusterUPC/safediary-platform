package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request to start a conversation session.
 */
public record StartConversationResource(@NotNull @Positive Long accountId, PersonalityTone tone) {
}
