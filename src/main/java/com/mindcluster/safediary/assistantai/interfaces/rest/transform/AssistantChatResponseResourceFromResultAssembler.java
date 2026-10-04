package com.mindcluster.safediary.assistantai.interfaces.rest.transform;

import com.mindcluster.safediary.assistantai.application.commandservices.ReflectionResult;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.AssistantChatResponseResource;

/**
 * Assembler from reflection result to the mobile-compatible chat response.
 */
public final class AssistantChatResponseResourceFromResultAssembler {

    private AssistantChatResponseResourceFromResultAssembler() {
    }

    public static AssistantChatResponseResource toResource(ReflectionResult result) {
        var emotion = result.userMessage().getEmotionTag();
        return new AssistantChatResponseResource(
                result.assistantMessage().getContent(),
                emotion == null ? null : emotion.name(),
                result.assistantMessage().getSentAt().toEpochMilli(),
                String.valueOf(result.session().getId()),
                result.riskLevel().name(),
                result.crisisResources().stream().map(CrisisHotlineResourceFromValueObjectAssembler::toResource).toList());
    }
}
