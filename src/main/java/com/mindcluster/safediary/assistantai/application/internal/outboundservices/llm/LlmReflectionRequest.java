package com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm;

import com.mindcluster.safediary.assistantai.domain.model.entities.ConversationMessage;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;

import java.util.List;

/**
 * Input of a reflection request.
 *
 * @param tone    personality tone chosen by the user
 * @param locale  answer language ("es" or "en")
 * @param history recent messages; the last one is the user message to answer
 */
public record LlmReflectionRequest(PersonalityTone tone, String locale, List<ConversationMessage> history) {
}
