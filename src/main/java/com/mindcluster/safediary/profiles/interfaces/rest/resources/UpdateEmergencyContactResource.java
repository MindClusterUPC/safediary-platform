package com.mindcluster.safediary.profiles.interfaces.rest.resources;

import com.mindcluster.safediary.profiles.domain.model.valueobjects.RelationshipType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateEmergencyContactResource(
        @NotBlank @Size(max = 120)
        @Schema(description = "Emergency contact full name", example = "Ana Gomez")
        String name,

        @NotNull
        @Schema(description = "Relationship type", example = "SPOUSE")
        RelationshipType relationship,

        @Schema(description = "Emergency contact phone", example = "+51912345678")
        String phoneNumber,

        @Schema(description = "Emergency contact email", example = "ana.gomez@example.com")
        String email
) {}
