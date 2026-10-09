package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.RatingSummary;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.RatingSummaryPersistenceEntity;

public final class RatingSummaryPersistenceAssembler {
    private RatingSummaryPersistenceAssembler() {}
    public static RatingSummary toDomain(RatingSummaryPersistenceEntity entity) {
        return new RatingSummary(entity.getClinicianId(), entity.getAverage(), entity.getReviewCount());
    }
    public static void copyToEntity(RatingSummary domain, RatingSummaryPersistenceEntity entity) {
        entity.setClinicianId(domain.clinicianId());
        entity.setAverage(domain.average());
        entity.setReviewCount(domain.reviewCount());
    }
}
