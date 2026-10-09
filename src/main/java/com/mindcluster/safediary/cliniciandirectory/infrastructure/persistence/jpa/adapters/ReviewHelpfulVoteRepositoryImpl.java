package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.ReviewHelpfulVote;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.domain.repositories.ReviewHelpfulVoteRepository;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.ReviewHelpfulVotePersistenceEntity;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.repositories.ReviewHelpfulVotePersistenceRepository;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.assemblers.ReviewHelpfulVotePersistenceAssembler;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository @RequiredArgsConstructor @Transactional(readOnly=true)
public class ReviewHelpfulVoteRepositoryImpl implements ReviewHelpfulVoteRepository {
    private final ReviewHelpfulVotePersistenceRepository persistence;

    public Optional<ReviewHelpfulVote> findByReviewIdAndAccountId(Long reviewId, Long accountId) { return persistence.findByReviewIdAndAccountId(reviewId, accountId).map(ReviewHelpfulVotePersistenceAssembler::toDomain); }
    public long countActive(Long reviewId) { return persistence.countByReviewIdAndActiveTrue(reviewId); }
    @Transactional public void deactivateByReviewId(Long reviewId) { var votes = persistence.findByReviewId(reviewId); votes.forEach(v -> v.setActive(false)); persistence.saveAllAndFlush(votes); }
    @Transactional public ReviewHelpfulVote save(ReviewHelpfulVote value) {
        var entity = value.id() == null ? new ReviewHelpfulVotePersistenceEntity() : persistence.findById(value.id()).orElseThrow();
        ReviewHelpfulVotePersistenceAssembler.copyToEntity(value, entity);
        var saved = persistence.saveAndFlush(entity);

        return ReviewHelpfulVotePersistenceAssembler.toDomain(saved);
    }
}
