package com.mindcluster.safediary.assistantai.domain.model.aggregates;

import com.mindcluster.safediary.assistantai.domain.model.entities.CognitiveDistortion;
import com.mindcluster.safediary.assistantai.domain.model.entities.ConversationMessage;
import com.mindcluster.safediary.assistantai.domain.model.events.*;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.MessageSender;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PlutchikEmotionTag;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.SessionStatus;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Conversation session aggregate root.
 * <p>
 * Represents a continuous reflective dialogue between the user and the AI companion.
 * A session in crisis keeps accepting messages; only a closed session rejects them.
 * </p>
 */
@Getter
public class ConversationSession extends AbstractDomainAggregateRoot<ConversationSession> {

    public static final int MAX_MESSAGE_LENGTH = 2000;

    private Long id;
    private Long accountId;
    private Instant startedAt;
    private Instant endedAt;
    private SessionStatus status;
    private PersonalityTone currentTone;
    private final List<ConversationMessage> messages = new ArrayList<>();

    /**
     * Starts a new session for the given account.
     */
    public ConversationSession(Long accountId, PersonalityTone tone) {
        if (accountId == null || accountId <= 0)
            throw new IllegalArgumentException("accountId must be a positive number");
        this.accountId = accountId;
        this.currentTone = tone != null ? tone : PersonalityTone.EMPATHIC;
        this.status = SessionStatus.ACTIVE;
        this.startedAt = Instant.now();
    }

    /**
     * Rebuilds a persisted session. Only used by persistence assemblers.
     */
    public ConversationSession(Long id, Long accountId, Instant startedAt, Instant endedAt,
                               SessionStatus status, PersonalityTone currentTone,
                               List<ConversationMessage> messages) {
        this.id = id;
        this.accountId = accountId;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.status = status;
        this.currentTone = currentTone;
        this.messages.addAll(messages);
    }

    public List<ConversationMessage> getMessages() {
        return List.copyOf(messages);
    }

    public List<ConversationMessage> getRecentMessages(int limit) {
        int from = Math.max(0, messages.size() - limit);
        return List.copyOf(messages.subList(from, messages.size()));
    }

    public ConversationMessage receiveUserMessage(String content) {
        ensureNotClosed();
        if (content == null || content.isBlank())
            throw new IllegalArgumentException("message content must not be blank");
        var trimmed = content.trim();
        if (trimmed.length() > MAX_MESSAGE_LENGTH)
            throw new IllegalArgumentException("message content exceeds " + MAX_MESSAGE_LENGTH + " characters");
        var message = ConversationMessage.newUserMessage(trimmed);
        messages.add(message);
        registerDomainEvent(new UserMessageReceivedEvent(id, Instant.now()));
        return message;
    }

    public void classifyLastUserMessage(PlutchikEmotionTag tag) {
        if (tag == null) return;
        findLastUserMessage().ifPresent(message -> {
            message.tagEmotion(tag);
            registerDomainEvent(new EmotionClassifiedEvent(id, tag, Instant.now()));
        });
    }

    public void recordDistortionsOnLastUserMessage(List<CognitiveDistortion> distortions) {
        if (distortions == null || distortions.isEmpty()) return;
        findLastUserMessage().ifPresent(message -> {
            for (var distortion : distortions) {
                message.addDistortion(distortion);
                registerDomainEvent(new CognitiveDistortionDetectedEvent(id, distortion.getType(), Instant.now()));
            }
        });
    }

    public ConversationMessage addAssistantReflection(String content) {
        ensureNotClosed();
        if (content == null || content.isBlank())
            throw new IllegalArgumentException("assistant reflection must not be blank");
        var message = ConversationMessage.newAiMessage(content.trim());
        messages.add(message);
        registerDomainEvent(new ReflectionGeneratedEvent(id, Instant.now()));
        return message;
    }

    public void flagForCrisis() {
        ensureNotClosed();
        if (status == SessionStatus.CRISIS_TRIGGERED) return;
        status = SessionStatus.CRISIS_TRIGGERED;
        registerDomainEvent(new CrisisProtocolActivatedEvent(id, accountId, Instant.now()));
    }

    public void changeTone(PersonalityTone tone) {
        ensureNotClosed();
        if (tone == null) throw new IllegalArgumentException("tone must not be null");
        currentTone = tone;
        registerDomainEvent(new PersonalityToneUpdatedEvent(id, tone, Instant.now()));
    }

    public void close() {
        ensureNotClosed();
        status = SessionStatus.CLOSED;
        endedAt = Instant.now();
        registerDomainEvent(new ConversationSessionClosedEvent(id, Instant.now()));
    }

    /**
     * Called by the repository right after a new session is persisted, once it has an id.
     */
    public void markAsStarted() {
        registerDomainEvent(new ConversationSessionStartedEvent(id, accountId, startedAt));
    }

    public boolean isInCrisis() {
        return status == SessionStatus.CRISIS_TRIGGERED;
    }

    private Optional<ConversationMessage> findLastUserMessage() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getSender() == MessageSender.USER) return Optional.of(messages.get(i));
        }
        return Optional.empty();
    }

    private void ensureNotClosed() {
        if (status == SessionStatus.CLOSED)
            throw new IllegalStateException("conversation session is closed");
    }
}
