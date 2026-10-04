package com.mindcluster.safediary.assistantai.domain.repositories;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;

import java.util.List;
import java.util.Optional;

/**
 * Conversation session repository port.
 */
public interface ConversationSessionRepository {

    Optional<ConversationSession> findById(Long id);

    /**
     * Most recent session of the account whose status is ACTIVE or CRISIS_TRIGGERED.
     */
    Optional<ConversationSession> findActiveByAccountId(Long accountId);

    /**
     * All sessions of the account, newest first.
     */
    List<ConversationSession> findAllByAccountId(Long accountId);

    ConversationSession save(ConversationSession session);
}
