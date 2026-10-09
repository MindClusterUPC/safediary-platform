package com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.events.DirectoryDomainEvent;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import java.time.Instant;

@Getter
public class ReviewReport extends AbstractDomainAggregateRoot<ReviewReport> {
    private final Long id;
    private final Long reviewId;
    private final Long reporterAccountId;
    private final ReportReason reason;
    private final String comment;
    private ReportStatus status;
    private final Instant reportedAt;
    private Long resolvedByAccountId;
    private Instant resolvedAt;
    private String resolution;

    public ReviewReport(Long id, Long reviewId, Long reporterId, ReportReason reason, String comment,
            ReportStatus status, Instant reportedAt, Long resolvedBy, Instant resolvedAt, String resolution) {
        if (reason == null || (comment != null && comment.length() > 1000))
            throw new IllegalArgumentException("A valid report reason and comment are required");
        this.id = id; this.reviewId = reviewId; this.reporterAccountId = reporterId; this.reason = reason;
        this.comment = comment; this.status = status; this.reportedAt = reportedAt;
        this.resolvedByAccountId = resolvedBy; this.resolvedAt = resolvedAt; this.resolution = resolution;
    }

    public static ReviewReport report(Long clinicianId, Long reviewId, Long reporterId, ReportReason reason, String comment) {
        var report = new ReviewReport(null, reviewId, reporterId, reason, comment, ReportStatus.PENDING,
                Instant.now(), null, null, null);
        report.registerDomainEvent(new DirectoryDomainEvent("ReviewReported", clinicianId, reviewId));
        return report;
    }

    public void resolve(Long clinicianId, Long moderator, boolean removeReview, String explanation) {
        if (status != ReportStatus.PENDING) throw new IllegalArgumentException("Report is already resolved");
        if (explanation == null || explanation.isBlank() || explanation.length() > 1000)
            throw new IllegalArgumentException("Resolution is required (maximum 1000 characters)");
        status = removeReview ? ReportStatus.REVIEW_REMOVED : ReportStatus.DISMISSED;
        resolvedByAccountId = moderator; resolvedAt = Instant.now(); resolution = explanation.trim();
        registerDomainEvent(new DirectoryDomainEvent("ReviewReportResolved", clinicianId, reviewId));
    }
}
