package com.mindcluster.safediary.profiles.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record RegisterPsychologistResource(
        @NotBlank @Size(max = 80)
        @Schema(description = "First name", example = "Dra. Sofia")
        String firstName,

        @NotBlank @Size(max = 80)
        @Schema(description = "Last name", example = "Valdivia")
        String lastName,

        @NotBlank @Email @Size(max = 254)
        @Schema(description = "Email address", example = "sofia.valdivia@safediary.com")
        String email,

        @NotNull @DecimalMin(value = "0.0")
        @Schema(description = "Cost per session", example = "120.00")
        BigDecimal costPerSession,

        @Min(value = 1)
        @Schema(description = "Session duration in minutes", example = "50")
        int durationMinutes,

        @Schema(description = "Currency", example = "PEN", defaultValue = "PEN")
        String currency,

        @Schema(description = "Allows Video Call sessions", example = "true")
        boolean videoCallEnabled,

        @Schema(description = "Allows Encrypted Chat sessions", example = "true")
        boolean encryptedChatEnabled,

        @Schema(description = "Professional biography and focus", example = "Psicologa clinica especializada en terapia cognitivo-conductual.")
        String bio,

        @Schema(description = "Categories / specialities", example = "[\"Ansiedad\", \"Depresion\", \"Autoestima\"]")
        List<String> categories,

        @Schema(description = "Certificate and degree titles", example = "[\"Licenciada en Psicologia - UNMSM\", \"Especialidad en TCC - Instituto Beck\"]")
        List<String> certificateTitles
) {}
