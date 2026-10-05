package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;
import jakarta.validation.constraints.NotNull;

/**
 * Request to change the personality tone of the AI companion.
 */
public record UpdateAiToneResource(@NotNull PersonalityTone tone) {
}
