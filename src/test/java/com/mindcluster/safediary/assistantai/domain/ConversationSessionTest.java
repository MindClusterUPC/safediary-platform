package com.mindcluster.safediary.assistantai.domain;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;
import com.mindcluster.safediary.assistantai.domain.model.entities.CognitiveDistortion;
import com.mindcluster.safediary.assistantai.domain.model.events.CognitiveDistortionDetectedEvent;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.DistortionType;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PlutchikEmotionTag;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.SessionStatus;
import org.junit.jupiter.api.Test;

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
}
