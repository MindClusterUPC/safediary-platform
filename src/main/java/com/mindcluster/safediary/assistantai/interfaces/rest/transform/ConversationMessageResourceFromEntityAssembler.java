package com.mindcluster.safediary.assistantai.interfaces.rest.transform;

import com.mindcluster.safediary.assistantai.domain.model.entities.ConversationMessage;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.CognitiveDistortionResource;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.ConversationMessageResource;

/**
 * Assembler from conversation message entity to resource.
 */
public final class ConversationMessageResourceFromEntityAssembler {

    private ConversationMessageResourceFromEntityAssembler() {
    }

    public static ConversationMessageResource toResource(ConversationMessage message) {
        var distortions = message.getDistortions().stream()
                .map(d -> new CognitiveDistortionResource(d.getType().name(), d.getEvidence(), d.getConfidence()))
                .toList();
        return new ConversationMessageResource(message.getId(), message.getSender().name(), message.getContent(),
                message.getEmotionTag() == null ? null : message.getEmotionTag().name(), message.getSentAt(), distortions);
    }
}
