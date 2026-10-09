package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.TrustScore;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.TrustScorePersistenceEntity;

public final class TrustScorePersistenceAssembler {
    private TrustScorePersistenceAssembler() {}
    public static TrustScore toDomain(TrustScorePersistenceEntity entity) {
        return new TrustScore(entity.getClinicianId(), entity.getScore(), entity.isSufficientData(), entity.getRatingFactor(), entity.getCompletedSessions(), entity.getCompletedMinutes(), entity.getReviewCount());
    }
    public static void copyToEntity(TrustScore domain, TrustScorePersistenceEntity entity) {
        entity.setClinicianId(domain.clinicianId());
        entity.setScore(domain.score());
        entity.setSufficientData(domain.sufficientData());
        entity.setRatingFactor(domain.ratingFactor());
        entity.setCompletedSessions(domain.completedSessions());
        entity.setCompletedMinutes(domain.completedMinutes());
        entity.setReviewCount(domain.reviewCount());
    }
}
