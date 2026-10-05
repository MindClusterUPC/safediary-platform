package com.mindcluster.safediary.assistantai.domain.repositories;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;

import java.time.Instant;
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

    /**
     * Sessions of the account that were alive at some point in [from, to).
     */
    List<ConversationSession> findAllByAccountIdOverlapping(Long accountId, Instant from, Instant to);

    /**
     * Accounts that had a session alive at some point in [from, to).
     */
    List<Long> findAccountIdsWithSessionsOverlapping(Instant from, Instant to);

    ConversationSession save(ConversationSession session);

    void delete(ConversationSession session);
}
