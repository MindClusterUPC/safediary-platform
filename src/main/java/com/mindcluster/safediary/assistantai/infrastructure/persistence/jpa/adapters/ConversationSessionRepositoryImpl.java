package com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ConversationSession;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.SessionStatus;
import com.mindcluster.safediary.assistantai.domain.repositories.ConversationSessionRepository;
import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.assemblers.ConversationSessionPersistenceAssembler;
import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.entities.ConversationMessagePersistenceEntity;
import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.entities.ConversationSessionPersistenceEntity;
import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.repositories.ConversationSessionPersistenceRepository;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository adapter that bridges the conversation session repository port with Spring Data JPA.
 */
@Repository
public class ConversationSessionRepositoryImpl implements ConversationSessionRepository {

    private static final List<SessionStatus> OPEN_STATUSES = List.of(SessionStatus.ACTIVE, SessionStatus.CRISIS_TRIGGERED);

    private final ConversationSessionPersistenceRepository persistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public ConversationSessionRepositoryImpl(ConversationSessionPersistenceRepository persistenceRepository,
                                             DomainEventPublisher domainEventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConversationSession> findById(Long id) {
        return persistenceRepository.findById(id).map(ConversationSessionPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConversationSession> findActiveByAccountId(Long accountId) {
        return persistenceRepository.findFirstByAccountIdAndStatusInOrderByStartedAtDesc(accountId, OPEN_STATUSES)
                .map(ConversationSessionPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationSession> findAllByAccountId(Long accountId) {
        return persistenceRepository.findAllByAccountIdOrderByStartedAtDesc(accountId).stream()
                .map(ConversationSessionPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationSession> findAllByAccountIdOverlapping(Long accountId, Instant from, Instant to) {
        return persistenceRepository.findOverlapping(accountId, from, to).stream()
                .map(ConversationSessionPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findAccountIdsWithSessionsOverlapping(Instant from, Instant to) {
        return persistenceRepository.findAccountIdsOverlapping(from, to);
    }

    @Override
    @Transactional
    public ConversationSession save(ConversationSession session) {
        boolean isNew = session.getId() == null;
        ConversationSessionPersistenceEntity entity = isNew
                ? new ConversationSessionPersistenceEntity()
                : persistenceRepository.findById(session.getId())
                    .orElseThrow(() -> new IllegalStateException("conversation session not found: " + session.getId()));
        entity.setAccountId(session.getAccountId());
        entity.setTitle(session.getTitle());
        entity.setStartedAt(session.getStartedAt());
        entity.setEndedAt(session.getEndedAt());
        entity.setStatus(session.getStatus());
        entity.setCurrentTone(session.getCurrentTone());

        var activeMessageIds = session.getMessages().stream()
                .map(m -> m.getId())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        entity.getMessages().removeIf(m -> m.getId() != null && !activeMessageIds.contains(m.getId()));

        var existingById = new HashMap<Long, ConversationMessagePersistenceEntity>();
        entity.getMessages().forEach(m -> existingById.put(m.getId(), m));
        for (var message : session.getMessages()) {
            if (message.getId() == null) {
                entity.addMessage(ConversationSessionPersistenceAssembler.toPersistenceFromMessage(message));
            } else if (existingById.containsKey(message.getId())) {
                existingById.get(message.getId()).setEmotionTag(message.getEmotionTag());
            }
        }
        var saved = persistenceRepository.saveAndFlush(entity);
        var result = ConversationSessionPersistenceAssembler.toDomainFromPersistence(saved);
        domainEventPublisher.publishAndClear(session);
        if (isNew) {
            result.markAsStarted();
            domainEventPublisher.publishAndClear(result);
        }
        return result;
    }

    @Override
    @Transactional
    public void delete(ConversationSession session) {
        if (session != null && session.getId() != null) {
            persistenceRepository.findById(session.getId()).ifPresent(persistenceRepository::delete);
        }
    }
}
