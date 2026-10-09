package com.mindcluster.safediary.profiles.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record UpdatePsychologistProfileResource(
        @NotNull @DecimalMin(value = "0.0")
        @Schema(description = "Cost per session", example = "150.00")
        BigDecimal costPerSession,

        @Min(value = 1)
        @Schema(description = "Session duration in minutes", example = "45")
        int durationMinutes,

        @Schema(description = "Currency", example = "PEN")
        String currency,

        @Schema(description = "Allows Video Call sessions", example = "true")
        boolean videoCallEnabled,

        @Schema(description = "Allows Encrypted Chat sessions", example = "false")
        boolean encryptedChatEnabled,

        @Schema(description = "Updated biography", example = "Psicologa clinica con enfoque humanista y gestalt.")
        String bio,

        @Schema(description = "Updated categories", example = "[\"Duelo\", \"Trauma\", \"Ansiedad\"]")
        List<String> categories,

        @Schema(description = "Updated certificates", example = "[\"Master en Psicoterapia\"]")
        List<String> certificateTitles
) {}
