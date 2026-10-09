package com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.ReviewStatus;
import com.mindcluster.safediary.cliniciandirectory.domain.model.events.DirectoryDomainEvent;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import java.time.Instant;

@Getter
public class Review extends AbstractDomainAggregateRoot<Review> {
    private final Long id;
    private final Long clinicianId;
    private final Long appointmentId;
    private final Long patientAccountId;
    private Integer rating;
    private String text;
    private ReviewStatus status;
    private final Instant publishedAt;

    public Review(Long id, Long clinicianId, Long appointmentId, Long patientAccountId,
                  Integer rating, String text, ReviewStatus status, Instant publishedAt) {
        if (status == ReviewStatus.PUBLISHED && (rating == null || rating < 1 || rating > 5))
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        if (text != null && text.length() > 2000) throw new IllegalArgumentException("Review text is too long");
        this.id = id; this.clinicianId = clinicianId; this.appointmentId = appointmentId;
        this.patientAccountId = patientAccountId; this.rating = rating; this.text = text;
        this.status = status; this.publishedAt = publishedAt;
    }

    public static Review publish(Long clinicianId, Long appointmentId, Long patientId, int rating, String text) {
        var review = new Review(null, clinicianId, appointmentId, patientId, rating, text,
                ReviewStatus.PUBLISHED, Instant.now());
        review.registerDomainEvent(new DirectoryDomainEvent("ReviewPublished", clinicianId, null));
        return review;
    }

    public void withdraw(boolean byAuthor) {
        status = byAuthor ? ReviewStatus.DELETED : ReviewStatus.REMOVED;
        rating = null; text = null;
        registerDomainEvent(new DirectoryDomainEvent("ReviewDeleted", clinicianId, id));
    }
}
