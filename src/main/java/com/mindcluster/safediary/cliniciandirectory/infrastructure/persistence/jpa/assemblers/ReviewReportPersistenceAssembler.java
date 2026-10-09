package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.ReviewReport;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.ReviewReportPersistenceEntity;

public final class ReviewReportPersistenceAssembler {
    private ReviewReportPersistenceAssembler() {}
    public static ReviewReport toDomain(ReviewReportPersistenceEntity entity) {
        return new ReviewReport(entity.getId(), entity.getReviewId(), entity.getReporterAccountId(), entity.getReason(), entity.getComment(), entity.getStatus(), entity.getReportedAt(), entity.getResolvedByAccountId(), entity.getResolvedAt(), entity.getResolution());
    }
    public static void copyToEntity(ReviewReport domain, ReviewReportPersistenceEntity entity) {
        entity.setReviewId(domain.getReviewId());
        entity.setReporterAccountId(domain.getReporterAccountId());
        entity.setReason(domain.getReason());
        entity.setComment(domain.getComment());
        entity.setStatus(domain.getStatus());
        entity.setReportedAt(domain.getReportedAt());
        entity.setResolvedByAccountId(domain.getResolvedByAccountId());
        entity.setResolvedAt(domain.getResolvedAt());
        entity.setResolution(domain.getResolution());
    }
}
