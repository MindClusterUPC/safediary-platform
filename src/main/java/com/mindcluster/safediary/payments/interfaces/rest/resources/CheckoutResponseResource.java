package com.mindcluster.safediary.payments.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Checkout initialization response")
public record CheckoutResponseResource(
        @Schema(description = "Patient account ID", example = "1")
        Long patientAccountId,

        @Schema(description = "Plan chosen", example = "TERRA")
        String plan,

        @Schema(description = "Stripe hosted checkout page URL (null if plan is FREE)", example = "https://checkout.stripe.com/c/pay/cs_test_...")
        String checkoutUrl,

        @Schema(description = "Stripe checkout session ID (null if plan is FREE)", example = "cs_test_...")
        String sessionId,

        @Schema(description = "True if external payment redirect is required; false for FREE plan", example = "true")
        boolean checkoutRequired,

        @Schema(description = "Status description message", example = "Checkout session created successfully.")
        String message
) {}
