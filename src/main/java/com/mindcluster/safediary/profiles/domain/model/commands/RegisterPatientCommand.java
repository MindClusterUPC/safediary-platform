package com.mindcluster.safediary.profiles.domain.model.commands;

import com.mindcluster.safediary.profiles.domain.model.valueobjects.RelationshipType;

public record RegisterPatientCommand(
        String firstName,
        String lastName,
        String email,
        String password,
        String emergencyContactName,
        RelationshipType emergencyContactRelationship,
        String emergencyContactPhone,
        String emergencyContactEmail
) {}
