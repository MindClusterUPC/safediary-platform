package com.mindcluster.safediary.payments.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request to cancel a recurring subscription")
public record CancelSubscriptionResource(
        @Schema(description = "Patient account ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "patientAccountId is required")
        @Positive(message = "patientAccountId must be positive")
        Long patientAccountId
) {}
