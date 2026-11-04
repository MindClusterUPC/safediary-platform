package com.mindcluster.safediary.profiles.domain.model.commands;

public record UpdatePatientProfileCommand(
        Long patientId,
        String firstName,
        String lastName
) {}
