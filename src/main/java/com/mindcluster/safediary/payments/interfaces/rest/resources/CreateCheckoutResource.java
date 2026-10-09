package com.mindcluster.safediary.payments.interfaces.rest.resources;

import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Request body to start a subscription or change plan")
public record CreateCheckoutResource(
        @Schema(description = "Patient account ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "patientAccountId is required")
        @Positive(message = "patientAccountId must be positive")
        Long patientAccountId,

        @Schema(description = "Target plan: FREE, TERRA ($19.99/mo), or ASTRUM ($100.00/yr)", example = "TERRA", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "plan is required")
        SubscriptionPlan plan,

        @Schema(description = "Custom redirect URL upon successful checkout", example = "http://localhost:8080/api/v1/payments/subscriptions/success")
        String successUrl,

        @Schema(description = "Custom redirect URL upon cancelled checkout", example = "http://localhost:8080/api/v1/payments/subscriptions/cancel")
        String cancelUrl
) {}
