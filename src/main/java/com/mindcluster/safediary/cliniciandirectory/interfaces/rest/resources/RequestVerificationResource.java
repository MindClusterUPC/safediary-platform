package com.mindcluster.safediary.cliniciandirectory.interfaces.rest.resources;

import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
public record RequestVerificationResource(@NotBlank @Size(max=120) String licenseNumber, @NotBlank @Size(max=100) String specialty,
        @NotBlank @Size(max=512) String documentRef) {}
