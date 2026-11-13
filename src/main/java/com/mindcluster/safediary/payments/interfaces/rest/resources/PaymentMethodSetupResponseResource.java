package com.mindcluster.safediary.payments.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Stripe Checkout setup session response")
public record PaymentMethodSetupResponseResource(
        @Schema(description = "Patient account ID", example = "5")
        Long patientAccountId,

        @Schema(description = "Stripe hosted Checkout URL in setup mode", example = "https://checkout.stripe.com/c/pay/cs_test_...")
        String checkoutUrl,

        @Schema(description = "Stripe Checkout session ID", example = "cs_test_...")
        String sessionId,

        @Schema(description = "Status description message", example = "Payment method setup session created successfully.")
        String message
) {}
