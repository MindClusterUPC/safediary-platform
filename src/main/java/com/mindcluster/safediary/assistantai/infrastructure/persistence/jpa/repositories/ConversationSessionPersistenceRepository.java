package com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.SessionStatus;
import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.entities.ConversationSessionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

    @Query("select s from ConversationSessionPersistenceEntity s where s.accountId = :accountId "
            + "and s.startedAt < :to and (s.endedAt is null or s.endedAt >= :from) order by s.startedAt asc")
    List<ConversationSessionPersistenceEntity> findOverlapping(@Param("accountId") Long accountId,
                                                               @Param("from") Instant from,
                                                               @Param("to") Instant to);

    @Query("select distinct s.accountId from ConversationSessionPersistenceEntity s "
            + "where s.startedAt < :to and (s.endedAt is null or s.endedAt >= :from)")
    List<Long> findAccountIdsOverlapping(@Param("from") Instant from, @Param("to") Instant to);
}
