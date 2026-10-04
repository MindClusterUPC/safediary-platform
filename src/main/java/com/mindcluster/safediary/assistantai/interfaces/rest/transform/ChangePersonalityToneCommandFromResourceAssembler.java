package com.mindcluster.safediary.assistantai.interfaces.rest.transform;

import com.mindcluster.safediary.assistantai.domain.model.commands.ChangePersonalityToneCommand;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.UpdateAiToneResource;

/**
 * Assembler from update AI tone resource to command.
 */
public final class ChangePersonalityToneCommandFromResourceAssembler {

    private ChangePersonalityToneCommandFromResourceAssembler() {
    }

    public static ChangePersonalityToneCommand toCommandFromResource(Long sessionId, UpdateAiToneResource resource) {
        return new ChangePersonalityToneCommand(sessionId, resource.tone());
    }
}
