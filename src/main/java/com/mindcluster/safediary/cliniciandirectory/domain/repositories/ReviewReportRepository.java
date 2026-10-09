package com.mindcluster.safediary.cliniciandirectory.domain.repositories;

import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.ReviewReport;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import java.util.List;
import java.util.Optional;

public interface ReviewReportRepository {
    Optional<Long> findReviewIdById(Long id);
    Optional<ReviewReport> findById(Long id);
    List<ReviewReport> findByStatus(ReportStatus status);
    boolean existsPending(Long reviewId, Long accountId);
    ReviewReport save(ReviewReport value);
}
