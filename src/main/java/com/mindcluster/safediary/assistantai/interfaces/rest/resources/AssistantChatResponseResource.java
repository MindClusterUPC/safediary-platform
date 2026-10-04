package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

import java.util.List;

/**
 * Chat response compatible with SafeDiary-Mobile (AssistantResponseDto).
 * The mobile app reads reply, sentiment and timestamp; the remaining fields are extra and safely ignored by Gson.
 *
 * @param timestamp epoch milliseconds of the assistant answer
 */
public record AssistantChatResponseResource(String reply, String sentiment, long timestamp, String conversationId,
                                            String riskLevel, List<CrisisHotlineResource> crisisResources) {
}
