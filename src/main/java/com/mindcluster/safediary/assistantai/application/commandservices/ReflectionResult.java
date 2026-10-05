package com.mindcluster.safediary.assistantai.application.commandservices;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;
import com.mindcluster.safediary.assistantai.domain.model.entities.ConversationMessage;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.CrisisHotline;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.RiskLevel;

import java.util.List;

/**
 * Outcome of sending a message to the AI companion.
 *
 * @param crisisResources emergency hotlines; empty when the session is not in crisis
 */
public record ReflectionResult(ConversationSession session, ConversationMessage userMessage,
                               ConversationMessage assistantMessage, RiskLevel riskLevel,
                               List<CrisisHotline> crisisResources) {
}
