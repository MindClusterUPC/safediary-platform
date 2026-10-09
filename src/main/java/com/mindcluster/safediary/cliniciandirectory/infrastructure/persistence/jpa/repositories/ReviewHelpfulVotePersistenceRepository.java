package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.ReviewHelpfulVotePersistenceEntity;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface ReviewHelpfulVotePersistenceRepository extends JpaRepository<ReviewHelpfulVotePersistenceEntity, Long> {
    Optional<ReviewHelpfulVotePersistenceEntity> findByReviewIdAndAccountId(Long reviewId, Long accountId);
    long countByReviewIdAndActiveTrue(Long reviewId);
    List<ReviewHelpfulVotePersistenceEntity> findByReviewId(Long reviewId);
}
