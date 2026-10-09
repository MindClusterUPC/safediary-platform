package com.mindcluster.safediary.profiles.domain.model.commands;

import com.mindcluster.safediary.profiles.domain.model.valueobjects.RelationshipType;

public record UpdateEmergencyContactCommand(
        Long patientId,
        String name,
        RelationshipType relationship,
        String phoneNumber,
        String email
) {}
