package com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.SessionStatus;
import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.entities.ConversationSessionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for conversation session persistence entities.
 */
@Repository
public interface ConversationSessionPersistenceRepository extends JpaRepository<ConversationSessionPersistenceEntity, Long> {

    Optional<ConversationSessionPersistenceEntity> findFirstByAccountIdAndStatusInOrderByStartedAtDesc(
            Long accountId, Collection<SessionStatus> statuses);

    List<ConversationSessionPersistenceEntity> findAllByAccountIdOrderByStartedAtDesc(Long accountId);
}
