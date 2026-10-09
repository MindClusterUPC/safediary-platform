package com.mindcluster.safediary.profiles.interfaces.rest.resources;

import com.mindcluster.safediary.profiles.domain.model.valueobjects.RelationshipType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterPatientResource(
        @NotBlank @Size(max = 80)
        @Schema(description = "First name", example = "Carlos")
        String firstName,

        @NotBlank @Size(max = 80)
        @Schema(description = "Last name", example = "Mendoza")
        String lastName,

        @NotBlank @Email @Size(max = 254)
        @Schema(description = "Email address", example = "carlos.mendoza@example.com")
        String email,

        @NotBlank @Size(min = 6, max = 100)
        @Schema(description = "Account password", example = "Secret123*")
        String password,

        @NotBlank @Size(max = 120)
        @Schema(description = "Emergency contact full name", example = "Maria Mendoza")
        String emergencyContactName,

        @NotNull
        @Schema(description = "Relationship type", example = "PARENT")
        RelationshipType emergencyContactRelationship,

        @Schema(description = "Emergency contact phone (either phone or email is required)", example = "+51987654321")
        String emergencyContactPhone,

        @Schema(description = "Emergency contact email (either phone or email is required)", example = "maria.mendoza@example.com")
        String emergencyContactEmail
) {}
