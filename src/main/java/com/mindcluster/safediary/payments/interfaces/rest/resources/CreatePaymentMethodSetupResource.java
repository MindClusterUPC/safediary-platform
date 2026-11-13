package com.mindcluster.safediary.payments.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Optional request payload to initiate a Stripe Checkout setup session for saving a payment method")
public record CreatePaymentMethodSetupResource(
        @Schema(description = "Patient account ID (optional; defaults to authenticated patient)", example = "5")
        Long patientAccountId,

        @Schema(description = "Optional custom redirect URL upon setup completion")
        String successUrl,

        @Schema(description = "Optional custom redirect URL if user cancels setup")
        String cancelUrl
) {}
