package com.mindcluster.safediary.profiles.interfaces.rest.resources;

import com.mindcluster.safediary.profiles.domain.model.valueobjects.RelationshipType;

public record PatientResponseResource(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        EmergencyContactResource emergencyContact,
        PlanStatusResource planStatus
) {
    public record EmergencyContactResource(
            String name,
            RelationshipType relationship,
            String phoneNumber,
            String email
    ) {}

    public record PlanStatusResource(
            String plan,
            String status,
            boolean active
    ) {}
}
