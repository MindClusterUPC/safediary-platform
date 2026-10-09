package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.Review;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.ReviewPersistenceEntity;

public final class ReviewPersistenceAssembler {
    private ReviewPersistenceAssembler() {}
    public static Review toDomain(ReviewPersistenceEntity entity) {
        return new Review(entity.getId(), entity.getClinicianId(), entity.getAppointmentId(), entity.getPatientAccountId(), entity.getRating(), entity.getText(), entity.getStatus(), entity.getPublishedAt());
    }
    public static void copyToEntity(Review domain, ReviewPersistenceEntity entity) {
        entity.setClinicianId(domain.getClinicianId());
        entity.setAppointmentId(domain.getAppointmentId());
        entity.setPatientAccountId(domain.getPatientAccountId());
        entity.setRating(domain.getRating());
        entity.setText(domain.getText());
        entity.setStatus(domain.getStatus());
        entity.setPublishedAt(domain.getPublishedAt());
    }
}
