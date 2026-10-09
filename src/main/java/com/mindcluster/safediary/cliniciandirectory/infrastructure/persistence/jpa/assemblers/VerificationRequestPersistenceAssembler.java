package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.VerificationRequest;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.VerificationRequestPersistenceEntity;

public final class VerificationRequestPersistenceAssembler {
    private VerificationRequestPersistenceAssembler() {}
    public static VerificationRequest toDomain(VerificationRequestPersistenceEntity entity) {
        return new VerificationRequest(entity.getId(), entity.getClinicianId(), new ProfessionalCredential(entity.getLicenseNumber(), entity.getSpecialty(), entity.getDocumentRef()), entity.getStatus(), entity.getSubmittedAt(), entity.getReviewedByAccountId(), entity.getReviewedAt(), entity.getRejectionReason());
    }
    public static void copyToEntity(VerificationRequest domain, VerificationRequestPersistenceEntity entity) {
        entity.setClinicianId(domain.getClinicianId());
        entity.setLicenseNumber(domain.getCredential().licenseNumber());
        entity.setSpecialty(domain.getCredential().specialty());
        entity.setDocumentRef(domain.getCredential().documentRef());
        entity.setStatus(domain.getStatus());
        entity.setSubmittedAt(domain.getSubmittedAt());
        entity.setReviewedByAccountId(domain.getReviewedByAccountId());
        entity.setReviewedAt(domain.getReviewedAt());
        entity.setRejectionReason(domain.getRejectionReason());
    }
}
