package com.mindcluster.safediary.assistantai.interfaces.rest.transform;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.ConversationSessionResource;

/**
 * Assembler from conversation session aggregate to resource.
 */
public final class ConversationSessionResourceFromEntityAssembler {

    private ConversationSessionResourceFromEntityAssembler() {
    }

    public static ConversationSessionResource toResource(ConversationSession session) {
        var messages = session.getMessages().stream()
                .map(ConversationMessageResourceFromEntityAssembler::toResource)
                .toList();
        return new ConversationSessionResource(session.getId(), session.getAccountId(), session.getStatus().name(),
                session.getCurrentTone().name(), session.getStartedAt(), session.getEndedAt(), messages);
    }
}
