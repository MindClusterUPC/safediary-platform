package com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.profiles.domain.model.aggregates.Patient;
import com.mindcluster.safediary.profiles.domain.model.entities.EmergencyContact;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.EmailAddress;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.FullName;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.PasswordHash;
import com.mindcluster.safediary.profiles.infrastructure.persistence.jpa.entities.PatientPersistenceEntity;

public final class PatientPersistenceAssembler {

    private PatientPersistenceAssembler() {}

    public static Patient toDomain(PatientPersistenceEntity entity) {
        if (entity == null) return null;

        EmergencyContact contact = EmergencyContact.of(
                entity.getEmergencyContactName(),
                entity.getEmergencyContactRelationship(),
                entity.getEmergencyContactPhone(),
                entity.getEmergencyContactEmail()
        );

        return new Patient(
                entity.getId(),
                FullName.of(entity.getFirstName(), entity.getLastName()),
                EmailAddress.of(entity.getEmail()),
                PasswordHash.of(entity.getPasswordHash()),
                contact
        );
    }

    public static void copyToEntity(Patient domain, PatientPersistenceEntity entity) {
        entity.setFirstName(domain.getFullName().firstName());
        entity.setLastName(domain.getFullName().lastName());
        entity.setEmail(domain.getEmail().value());
        entity.setPasswordHash(domain.getPassword().value());
        entity.setEmergencyContactName(domain.getEmergencyContact().getName());
        entity.setEmergencyContactRelationship(domain.getEmergencyContact().getRelationship());
        entity.setEmergencyContactPhone(domain.getEmergencyContact().getPhoneNumber());
        entity.setEmergencyContactEmail(
                domain.getEmergencyContact().getEmail() != null ? domain.getEmergencyContact().getEmail().value() : null
        );
    }
}
