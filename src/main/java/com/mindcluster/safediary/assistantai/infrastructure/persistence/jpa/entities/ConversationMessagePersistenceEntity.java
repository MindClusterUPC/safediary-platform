package com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.MessageSender;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PlutchikEmotionTag;
import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA persistence entity for conversation messages.
 */
@Entity
@Table(name = "conversation_messages")
@Getter
@Setter
@NoArgsConstructor
public class ConversationMessagePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id")
    private ConversationSessionPersistenceEntity session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MessageSender sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PlutchikEmotionTag emotionTag;

    @Column(nullable = false)
    private Instant sentAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CognitiveDistortionPersistenceEntity> distortions = new ArrayList<>();

    public void addDistortion(CognitiveDistortionPersistenceEntity distortion) {
        distortion.setMessage(this);
        distortions.add(distortion);
    }
}
