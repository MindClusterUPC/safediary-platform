package com.mindcluster.safediary.assistantai.interfaces.rest.transform;

import com.mindcluster.safediary.assistantai.application.commandservices.ReflectionResult;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.ReflectionResponseResource;

/**
 * Assembler from reflection result to resource.
 */
public final class ReflectionResponseResourceFromResultAssembler {

    private ReflectionResponseResourceFromResultAssembler() {
    }

    public static ReflectionResponseResource toResource(ReflectionResult result) {
        return new ReflectionResponseResource(result.session().getId(), result.session().getStatus().name(),
                ConversationMessageResourceFromEntityAssembler.toResource(result.userMessage()),
                ConversationMessageResourceFromEntityAssembler.toResource(result.assistantMessage()),
                result.riskLevel().name(),
                result.crisisResources().stream().map(CrisisHotlineResourceFromValueObjectAssembler::toResource).toList());
    }
}
