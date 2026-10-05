package com.mindcluster.safediary.assistantai.interfaces.rest.transform;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.CrisisHotline;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.MessageSender;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.AssistantChatMessageResource;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.AssistantConversationResource;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.AssistantConversationSummaryResource;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.CrisisHotlineResource;

import java.time.Instant;
import java.util.List;

/**
 * Assembler from conversation sessions to the mobile chat history resources.
 */
public final class AssistantConversationResourceFromEntityAssembler {

    private static final int MAX_TITLE_LENGTH = 48;

    private AssistantConversationResourceFromEntityAssembler() {
    }

    public static AssistantConversationSummaryResource toSummaryResource(ConversationSession session) {
        return new AssistantConversationSummaryResource(String.valueOf(session.getId()), titleOf(session),
                session.getStatus().name(), session.getStartedAt(), lastActivityOf(session));
    }

    public static AssistantConversationResource toResource(ConversationSession session, List<CrisisHotline> hotlines) {
        var messages = session.getMessages().stream()
                .map(m -> new AssistantChatMessageResource(m.getId(), m.getSender() == MessageSender.USER ? "user" : "assistant",
                        m.getContent(), m.getSentAt()))
                .toList();
        var crisisResources = session.isInCrisis()
                ? hotlines.stream().map(CrisisHotlineResourceFromValueObjectAssembler::toResource).toList()
                : List.<CrisisHotlineResource>of();
        return new AssistantConversationResource(String.valueOf(session.getId()), titleOf(session),
                session.getStatus().name(), messages, crisisResources);
    }

    public static Instant lastActivityOf(ConversationSession session) {
        var messages = session.getMessages();
        return messages.isEmpty() ? session.getStartedAt() : messages.get(messages.size() - 1).getSentAt();
    }

    private static String titleOf(ConversationSession session) {
        return session.getMessages().stream()
                .filter(m -> m.getSender() == MessageSender.USER)
                .findFirst()
                .map(m -> m.getContent().replace('\n', ' ').trim())
                .map(text -> text.length() > MAX_TITLE_LENGTH ? text.substring(0, MAX_TITLE_LENGTH).trim() + "…" : text)
                .orElse(null);
    }
}
