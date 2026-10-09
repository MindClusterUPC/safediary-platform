package com.mindcluster.safediary.cliniciandirectory.domain.services;

import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.Review;
import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.ReviewStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class ReputationCalculator {
    private ReputationCalculator() {}
    public static RatingSummary rating(Long clinicianId, List<Review> reviews) {
        var ratings = reviews.stream().filter(r -> r.getStatus() == ReviewStatus.PUBLISHED).map(Review::getRating).toList();
        BigDecimal average = ratings.isEmpty() ? null : BigDecimal.valueOf(ratings.stream().mapToInt(Integer::intValue).sum())
                .divide(BigDecimal.valueOf(ratings.size()), 2, RoundingMode.HALF_UP);
        return new RatingSummary(clinicianId, average, ratings.size());
    }
    public static TrustScore trust(RatingSummary rating, List<CompletedSession> sessions) {
        boolean sufficient = rating.reviewCount() >= 3;
        BigDecimal factor = rating.average() == null ? null : rating.average().multiply(BigDecimal.valueOf(20));
        return new TrustScore(rating.clinicianId(), sufficient ? factor : null, sufficient, factor,
                sessions.size(), sessions.stream().mapToLong(CompletedSession::durationMinutes).sum(), rating.reviewCount());
    }
}
