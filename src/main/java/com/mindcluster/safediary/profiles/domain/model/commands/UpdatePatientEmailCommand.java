package com.mindcluster.safediary.profiles.domain.model.commands;

public record UpdatePatientEmailCommand(
        Long patientId,
        String newEmail,
        String currentPassword
) {}
