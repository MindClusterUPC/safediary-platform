package com.mindcluster.safediary.profiles.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdatePatientEmailResource(
        @NotBlank(message = "New email is required")
        @Email(message = "Invalid email format")
        String newEmail,

        @NotBlank(message = "Current password is required")
        String currentPassword
) {}
