package com.mindcluster.safediary.profiles.interfaces.rest.resources;

import com.mindcluster.safediary.profiles.domain.model.valueobjects.RelationshipType;

public record PatientResponseResource(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        EmergencyContactResource emergencyContact
) {
    public record EmergencyContactResource(
            String name,
            RelationshipType relationship,
            String phoneNumber,
            String email
    ) {}
}
