package com.mindcluster.safediary.cliniciandirectory.interfaces.rest.resources;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
public record UpsertProfessionalProfileResource(@NotBlank @Size(max=160) String displayName, @NotBlank @Size(max=120) String professionalTitle,
        @NotBlank @Size(max=2000) String bio, @Size(max=512) String bannerRef,
        @NotEmpty @Size(max=20) List<@NotBlank @Size(max=100) String> specialties,
        @NotNull @DecimalMin("0") @Digits(integer=8, fraction=2) BigDecimal amount,
        @NotBlank @Size(min=3, max=3) String currency, @Min(1) @Max(480) int durationMinutes) {}
