package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

import java.util.List;

/**
 * Full mobile chat conversation. Crisis resources are included when the conversation is in crisis.
 */
public record AssistantConversationResource(String conversationId, String title, String status,
                                            List<AssistantChatMessageResource> messages,
                                            List<CrisisHotlineResource> crisisResources) {
}
