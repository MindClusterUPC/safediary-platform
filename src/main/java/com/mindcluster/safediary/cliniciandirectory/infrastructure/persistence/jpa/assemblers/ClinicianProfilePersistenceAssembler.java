package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.ClinicianProfile;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.ClinicianProfilePersistenceEntity;

public final class ClinicianProfilePersistenceAssembler {
    private ClinicianProfilePersistenceAssembler() {}
    public static ClinicianProfile toDomain(ClinicianProfilePersistenceEntity entity, ConsultationRate rate) {
        return new ClinicianProfile(entity.getId(), entity.getAccountId(), entity.getDisplayName(), entity.getProfessionalTitle(), entity.getBio(), entity.getBannerRef(), entity.getSpecialties(), rate, entity.getVerificationStatus(), entity.getPublicationStatus());
    }
    public static void copyToEntity(ClinicianProfile domain, ClinicianProfilePersistenceEntity entity) {
        entity.setAccountId(domain.getAccountId());
        entity.setDisplayName(domain.getDisplayName());
        entity.setProfessionalTitle(domain.getProfessionalTitle());
        entity.setBio(domain.getBio());
        entity.setBannerRef(domain.getBannerRef());
        entity.setVerificationStatus(domain.getVerificationStatus());
        entity.setPublicationStatus(domain.getPublicationStatus());
        entity.setSpecialties(new java.util.ArrayList<>(domain.getSpecialties()));
    }
}
