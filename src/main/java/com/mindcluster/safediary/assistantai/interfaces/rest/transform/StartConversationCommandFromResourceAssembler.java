package com.mindcluster.safediary.assistantai.interfaces.rest.transform;

import com.mindcluster.safediary.assistantai.domain.model.commands.StartConversationCommand;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.StartConversationResource;

/**
 * Assembler from start conversation resource to command.
 */
public final class StartConversationCommandFromResourceAssembler {

    private StartConversationCommandFromResourceAssembler() {
    }

    public static StartConversationCommand toCommandFromResource(StartConversationResource resource) {
        return new StartConversationCommand(resource.accountId(), resource.tone());
    }
}
