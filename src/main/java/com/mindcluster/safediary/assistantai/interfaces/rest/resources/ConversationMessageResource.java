package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

import java.time.Instant;
import java.util.List;

/**
 * Conversation message with its emotion tag and detected distortions.
 */
public record ConversationMessageResource(Long id, String sender, String content, String emotionTag, Instant sentAt, List<CognitiveDistortionResource> distortions) {
}
