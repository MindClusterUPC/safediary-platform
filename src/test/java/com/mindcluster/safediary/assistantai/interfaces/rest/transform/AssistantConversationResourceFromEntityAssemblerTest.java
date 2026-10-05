package com.mindcluster.safediary.assistantai.interfaces.rest.transform;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;
import com.mindcluster.safediary.assistantai.domain.model.entities.ConversationMessage;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.MessageSender;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.SessionStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AssistantConversationResourceFromEntityAssemblerTest {

    @Test
    void usesCustomTitleWhenPresent() {
        var msg = new ConversationMessage(10L, MessageSender.USER, "Primer mensaje del usuario", null, Instant.now(), List.of());
        var session = new ConversationSession(1L, 100L, "Título personalizado", Instant.now(), null,
                SessionStatus.ACTIVE, PersonalityTone.EMPATHIC, List.of(msg));

        var summary = AssistantConversationResourceFromEntityAssembler.toSummaryResource(session);
        var detail = AssistantConversationResourceFromEntityAssembler.toResource(session, List.of());

        assertThat(summary.title()).isEqualTo("Título personalizado");
        assertThat(detail.title()).isEqualTo("Título personalizado");
        assertThat(detail.messages()).hasSize(1);
        assertThat(detail.messages().get(0).id()).isEqualTo(10L);
    }

    @Test
    void fallsBackToFirstUserMessageWhenTitleIsNull() {
        var msg = new ConversationMessage(10L, MessageSender.USER, "Primer mensaje del usuario", null, Instant.now(), List.of());
        var session = new ConversationSession(1L, 100L, null, Instant.now(), null,
                SessionStatus.ACTIVE, PersonalityTone.EMPATHIC, List.of(msg));

        var summary = AssistantConversationResourceFromEntityAssembler.toSummaryResource(session);
        var detail = AssistantConversationResourceFromEntityAssembler.toResource(session, List.of());

        assertThat(summary.title()).isEqualTo("Primer mensaje del usuario");
        assertThat(detail.title()).isEqualTo("Primer mensaje del usuario");
    }
}
