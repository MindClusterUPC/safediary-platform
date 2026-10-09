package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.ReviewHelpfulVote;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.ReviewHelpfulVotePersistenceEntity;

public final class ReviewHelpfulVotePersistenceAssembler {
    private ReviewHelpfulVotePersistenceAssembler() {}
    public static ReviewHelpfulVote toDomain(ReviewHelpfulVotePersistenceEntity entity) {
        return new ReviewHelpfulVote(entity.getId(), entity.getReviewId(), entity.getAccountId(), entity.isActive());
    }
    public static void copyToEntity(ReviewHelpfulVote domain, ReviewHelpfulVotePersistenceEntity entity) {
        entity.setReviewId(domain.reviewId());
        entity.setAccountId(domain.accountId());
        entity.setActive(domain.active());
    }
}
