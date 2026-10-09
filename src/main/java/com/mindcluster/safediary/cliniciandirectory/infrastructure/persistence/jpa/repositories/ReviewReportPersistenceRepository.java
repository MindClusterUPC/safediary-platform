package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.ReviewReportPersistenceEntity;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface ReviewReportPersistenceRepository extends JpaRepository<ReviewReportPersistenceEntity, Long> {
    @Query("select e.reviewId from ReviewReportPersistenceEntity e where e.id = :id")
    Optional<Long> findReviewIdById(@Param("id") Long id);
    List<ReviewReportPersistenceEntity> findByStatusOrderByReportedAtAsc(ReportStatus status);
    boolean existsByReviewIdAndReporterAccountIdAndStatus(Long reviewId, Long accountId, ReportStatus status);
}
