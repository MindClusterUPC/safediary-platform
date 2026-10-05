package com.mindcluster.safediary.assistantai.domain;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;
import com.mindcluster.safediary.assistantai.domain.model.entities.CognitiveDistortion;
import com.mindcluster.safediary.assistantai.domain.model.entities.ConversationMessage;
import com.mindcluster.safediary.assistantai.domain.model.events.CognitiveDistortionDetectedEvent;
import com.mindcluster.safediary.assistantai.domain.model.events.CrisisProtocolActivatedEvent;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.DistortionType;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.MessageSender;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PlutchikEmotionTag;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.SessionStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConversationSessionTest {

    @Test
    void newSessionIsActiveWithEmpathicToneByDefault() {
        var session = new ConversationSession(1L, null);

        assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
        assertThat(session.getCurrentTone()).isEqualTo(PersonalityTone.EMPATHIC);
    }

    @Test
    void rejectsInvalidAccountId() {
        assertThatThrownBy(() -> new ConversationSession(null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ConversationSession(0L, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlankAndTooLongMessages() {
        var session = new ConversationSession(1L, null);

        assertThatThrownBy(() -> session.receiveUserMessage("  ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> session.receiveUserMessage("a".repeat(ConversationSession.MAX_MESSAGE_LENGTH + 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void closedSessionRejectsMessages() {
        var session = new ConversationSession(1L, null);
        session.close();

        assertThatThrownBy(() -> session.receiveUserMessage("hola")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sessionInCrisisKeepsAcceptingMessages() {
        var session = new ConversationSession(1L, null);
        session.flagForCrisis();

        session.receiveUserMessage("sigo aquí");

        assertThat(session.getStatus()).isEqualTo(SessionStatus.CRISIS_TRIGGERED);
        assertThat(session.getMessages()).hasSize(1);
        assertThat(session.domainEvents()).hasAtLeastOneElementOfType(CrisisProtocolActivatedEvent.class);
    }

    @Test
    void emotionAndDistortionsAreRecordedOnLastUserMessage() {
        var session = new ConversationSession(1L, null);
        session.receiveUserMessage("todo saldrá mal");
        session.addAssistantReflection("Te escucho");

        session.classifyLastUserMessage(PlutchikEmotionTag.JOY);
        session.recordDistortionsOnLastUserMessage(
                List.of(new CognitiveDistortion(null, DistortionType.CATASTROPHIZING, "todo saldrá mal", 0.8)));

        var userMessage = session.getMessages().get(0);
        assertThat(userMessage.getEmotionTag()).isEqualTo(PlutchikEmotionTag.JOY);
        assertThat(userMessage.getDistortions()).extracting(CognitiveDistortion::getType)
                .containsExactly(DistortionType.CATASTROPHIZING);
        assertThat(session.getMessages().get(1).getDistortions()).isEmpty();
        assertThat(session.domainEvents()).hasAtLeastOneElementOfType(CognitiveDistortionDetectedEvent.class);
    }

    @Test
    void renameWithValidTitleSetsTrimmedTitle() {
        var session = new ConversationSession(1L, null);
        session.rename("  Mi sesión de desahogo  ");

        assertThat(session.getTitle()).isEqualTo("Mi sesión de desahogo");
    }

    @Test
    void renameRejectsBlankTitle() {
        var session = new ConversationSession(1L, null);

        assertThatThrownBy(() -> session.rename(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
        assertThatThrownBy(() -> session.rename("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void renameRejectsTooLongTitle() {
        var session = new ConversationSession(1L, null);

        assertThatThrownBy(() -> session.rename("a".repeat(81)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("80");
    }

    @Test
    void truncateFromRemovesTargetUserMessageAndLaterMessages() {
        var msg1 = new ConversationMessage(1L, MessageSender.USER, "Hola", null, Instant.now(), List.of());
        var msg2 = new ConversationMessage(2L, MessageSender.AI, "Hola, ¿cómo estás?", null, Instant.now(), List.of());
        var msg3 = new ConversationMessage(3L, MessageSender.USER, "Me siento abrumado", null, Instant.now(), List.of());
        var msg4 = new ConversationMessage(4L, MessageSender.AI, "Comprendo...", null, Instant.now(), List.of());
        var session = new ConversationSession(10L, 1L, null, Instant.now(), null, SessionStatus.ACTIVE, PersonalityTone.EMPATHIC,
                List.of(msg1, msg2, msg3, msg4));

        session.truncateFrom(3L);

        assertThat(session.getMessages()).extracting(ConversationMessage::getId).containsExactly(1L, 2L);

        session.truncateFrom(1L);

        assertThat(session.getMessages()).isEmpty();
    }

    @Test
    void truncateFromFailsWhenTargetIsAiMessage() {
        var msg1 = new ConversationMessage(1L, MessageSender.USER, "Hola", null, Instant.now(), List.of());
        var msg2 = new ConversationMessage(2L, MessageSender.AI, "Hola, ¿cómo estás?", null, Instant.now(), List.of());
        var session = new ConversationSession(10L, 1L, null, Instant.now(), null, SessionStatus.ACTIVE, PersonalityTone.EMPATHIC,
                List.of(msg1, msg2));

        assertThatThrownBy(() -> session.truncateFrom(2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("user message");
    }

    @Test
    void truncateFromFailsWhenIdDoesNotExist() {
        var msg1 = new ConversationMessage(1L, MessageSender.USER, "Hola", null, Instant.now(), List.of());
        var session = new ConversationSession(10L, 1L, null, Instant.now(), null, SessionStatus.ACTIVE, PersonalityTone.EMPATHIC,
                List.of(msg1));

        assertThatThrownBy(() -> session.truncateFrom(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void removeLastAssistantReplyRemovesAiMessageAndReturnsLastUserMessage() {
        var msg1 = new ConversationMessage(1L, MessageSender.USER, "Hola", null, Instant.now(), List.of());
        var msg2 = new ConversationMessage(2L, MessageSender.AI, "Hola, ¿cómo estás?", null, Instant.now(), List.of());
        var session = new ConversationSession(10L, 1L, null, Instant.now(), null, SessionStatus.ACTIVE, PersonalityTone.EMPATHIC,
                List.of(msg1, msg2));

        var lastUser = session.removeLastAssistantReply();

        assertThat(lastUser.getId()).isEqualTo(1L);
        assertThat(session.getMessages()).extracting(ConversationMessage::getId).containsExactly(1L);
    }

    @Test
    void removeLastAssistantReplyKeepsUserMessageWhenLastMessageIsUser() {
        var msg1 = new ConversationMessage(1L, MessageSender.USER, "Hola", null, Instant.now(), List.of());
        var session = new ConversationSession(10L, 1L, null, Instant.now(), null, SessionStatus.ACTIVE, PersonalityTone.EMPATHIC,
                List.of(msg1));

        var lastUser = session.removeLastAssistantReply();

        assertThat(lastUser.getId()).isEqualTo(1L);
        assertThat(session.getMessages()).hasSize(1);
    }

    @Test
    void removeLastAssistantReplyThrowsWhenNoUserMessageExists() {
        var emptySession = new ConversationSession(1L, null);
        assertThatThrownBy(emptySession::removeLastAssistantReply)
                .isInstanceOf(IllegalStateException.class);

        var aiOnlyMsg = new ConversationMessage(1L, MessageSender.AI, "Hola", null, Instant.now(), List.of());
        var aiOnlySession = new ConversationSession(10L, 1L, null, Instant.now(), null, SessionStatus.ACTIVE, PersonalityTone.EMPATHIC,
                List.of(aiOnlyMsg));
        assertThatThrownBy(aiOnlySession::removeLastAssistantReply)
                .isInstanceOf(IllegalStateException.class);
    }
}
