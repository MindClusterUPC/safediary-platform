package com.mindcluster.safediary.assistantai.interfaces.rest.transform;

import com.mindcluster.safediary.assistantai.domain.model.commands.SendTextMessageCommand;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.SendTextMessageResource;

/**
 * Assembler from send text message resource to command.
 */
public final class SendTextMessageCommandFromResourceAssembler {

    private SendTextMessageCommandFromResourceAssembler() {
    }

    public static SendTextMessageCommand toCommandFromResource(Long sessionId, SendTextMessageResource resource) {
        return new SendTextMessageCommand(sessionId, resource.content(), resource.locale());
    }
}
