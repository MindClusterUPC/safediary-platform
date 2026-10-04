package com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm;

/**
 * Outbound port to the language model that powers the AI companion.
 * <p>
 * Implementations are anti-corruption layers: they translate the provider contract into SafeDiary's model.
 * It throws {@link LlmUnavailableException} when the provider fails.
 * </p>
 */
public interface AssistantLanguageModel {

    /**
     * Generates the reflection for the last user message, classifying its emotion, risk and distortions.
     */
    LlmReply generateReflection(LlmReflectionRequest request);
}
