package com.mindcluster.safediary.assistantai.domain.model.entities;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.MessageSender;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PlutchikEmotionTag;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Message exchanged inside a conversation session, either from the user or from the AI companion.
 */
@Getter
public class ConversationMessage {

    private final Long id;
    private final MessageSender sender;
    private final String content;
    private PlutchikEmotionTag emotionTag;
    private final Instant sentAt;
    private final List<CognitiveDistortion> distortions = new ArrayList<>();

    public ConversationMessage(Long id, MessageSender sender, String content, PlutchikEmotionTag emotionTag,
                               Instant sentAt, List<CognitiveDistortion> distortions) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.emotionTag = emotionTag;
        this.sentAt = sentAt;
        if (distortions != null) this.distortions.addAll(distortions);
    }

    public static ConversationMessage newUserMessage(String content) {
        return new ConversationMessage(null, MessageSender.USER, content, null, Instant.now(), List.of());
    }

    public static ConversationMessage newAiMessage(String content) {
        return new ConversationMessage(null, MessageSender.AI, content, null, Instant.now(), List.of());
    }

    public List<CognitiveDistortion> getDistortions() {
        return List.copyOf(distortions);
    }

    public void tagEmotion(PlutchikEmotionTag tag) {
        this.emotionTag = tag;
    }

    public void addDistortion(CognitiveDistortion distortion) {
        if (sender != MessageSender.USER)
            throw new IllegalStateException("only user messages can contain cognitive distortions");
        distortions.add(distortion);
    }
}
