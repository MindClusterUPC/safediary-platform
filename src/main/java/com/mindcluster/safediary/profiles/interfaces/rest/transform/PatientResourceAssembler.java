package com.mindcluster.safediary.profiles.interfaces.rest.transform;

import com.mindcluster.safediary.profiles.domain.model.aggregates.Patient;
import com.mindcluster.safediary.profiles.interfaces.rest.resources.PatientResponseResource;

public final class PatientResourceAssembler {

    private PatientResourceAssembler() {}

    public static PatientResponseResource toResource(Patient patient) {
        if (patient == null) return null;

        var contact = patient.getEmergencyContact();
        var contactResource = new PatientResponseResource.EmergencyContactResource(
                contact.getName(),
                contact.getRelationship(),
                contact.getPhoneNumber(),
                contact.getEmail() != null ? contact.getEmail().value() : null
        );

        return new PatientResponseResource(
                patient.getId(),
                patient.getFullName().firstName(),
                patient.getFullName().lastName(),
                patient.getFullName().toCombinedString(),
                patient.getEmail().value(),
                contactResource
        );
    }
}
