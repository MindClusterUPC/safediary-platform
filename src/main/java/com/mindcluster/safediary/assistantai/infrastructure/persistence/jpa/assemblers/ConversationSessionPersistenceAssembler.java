package com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;
import com.mindcluster.safediary.assistantai.domain.model.entities.CognitiveDistortion;
import com.mindcluster.safediary.assistantai.domain.model.entities.ConversationMessage;
import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.entities.CognitiveDistortionPersistenceEntity;
import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.entities.ConversationMessagePersistenceEntity;
import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.entities.ConversationSessionPersistenceEntity;

/**
 * Static assembler between conversation session domain and persistence representations.
 */
public final class ConversationSessionPersistenceAssembler {

    private ConversationSessionPersistenceAssembler() {
    }

    public static ConversationSession toDomainFromPersistence(ConversationSessionPersistenceEntity entity) {
        var messages = entity.getMessages().stream()
                .map(ConversationSessionPersistenceAssembler::toDomainFromPersistence)
                .toList();
        return new ConversationSession(entity.getId(), entity.getAccountId(), entity.getTitle(), entity.getStartedAt(),
                entity.getEndedAt(), entity.getStatus(), entity.getCurrentTone(), messages);
    }

    /**
     * Builds the persistence entity of a new (not yet persisted) message, including its distortions.
     */
    public static ConversationMessagePersistenceEntity toPersistenceFromMessage(ConversationMessage message) {
        var entity = new ConversationMessagePersistenceEntity();
        entity.setSender(message.getSender());
        entity.setContent(message.getContent());
        entity.setEmotionTag(message.getEmotionTag());
        entity.setSentAt(message.getSentAt());
        for (var distortion : message.getDistortions()) {
            var distortionEntity = new CognitiveDistortionPersistenceEntity();
            distortionEntity.setType(distortion.getType());
            distortionEntity.setEvidence(distortion.getEvidence());
            distortionEntity.setConfidence(distortion.getConfidence());
            entity.addDistortion(distortionEntity);
        }
        return entity;
    }

    private static ConversationMessage toDomainFromPersistence(ConversationMessagePersistenceEntity entity) {
        var distortions = entity.getDistortions().stream()
                .map(d -> new CognitiveDistortion(d.getId(), d.getType(), d.getEvidence(), d.getConfidence()))
                .toList();
        return new ConversationMessage(entity.getId(), entity.getSender(), entity.getContent(),
                entity.getEmotionTag(), entity.getSentAt(), distortions);
    }
}
