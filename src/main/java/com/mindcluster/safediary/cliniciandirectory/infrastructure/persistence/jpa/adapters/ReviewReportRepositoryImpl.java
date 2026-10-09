package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.ReviewReport;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.domain.repositories.ReviewReportRepository;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.ReviewReportPersistenceEntity;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.repositories.ReviewReportPersistenceRepository;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.assemblers.ReviewReportPersistenceAssembler;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository @RequiredArgsConstructor @Transactional(readOnly=true)
public class ReviewReportRepositoryImpl implements ReviewReportRepository {
    public Optional<Long> findReviewIdById(Long id) { return persistence.findReviewIdById(id); }
    private final ReviewReportPersistenceRepository persistence;
    private final DomainEventPublisher events;
    public Optional<ReviewReport> findById(Long id) { return persistence.findById(id).map(ReviewReportPersistenceAssembler::toDomain); }
    public List<ReviewReport> findByStatus(ReportStatus status) { return persistence.findByStatusOrderByReportedAtAsc(status).stream().map(ReviewReportPersistenceAssembler::toDomain).toList(); }
    public boolean existsPending(Long reviewId, Long accountId) { return persistence.existsByReviewIdAndReporterAccountIdAndStatus(reviewId, accountId, ReportStatus.PENDING); }
    @Transactional public ReviewReport save(ReviewReport value) {
        var entity = value.getId() == null ? new ReviewReportPersistenceEntity() : persistence.findById(value.getId()).orElseThrow();
        ReviewReportPersistenceAssembler.copyToEntity(value, entity);
        var saved = persistence.saveAndFlush(entity);
        events.publishAndClear(value);
        return ReviewReportPersistenceAssembler.toDomain(saved);
    }
}
