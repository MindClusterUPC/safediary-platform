package com.mindcluster.safediary.cliniciandirectory.domain.repositories;

import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.ReviewHelpfulVote;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import java.util.List;
import java.util.Optional;

public interface ReviewHelpfulVoteRepository {
    Optional<ReviewHelpfulVote> findByReviewIdAndAccountId(Long reviewId, Long accountId);
    long countActive(Long reviewId);
    void deactivateByReviewId(Long reviewId);
    ReviewHelpfulVote save(ReviewHelpfulVote value);
}
