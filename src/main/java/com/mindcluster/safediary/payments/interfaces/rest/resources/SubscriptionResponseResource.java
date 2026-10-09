package com.mindcluster.safediary.payments.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Patient subscription details")
public record SubscriptionResponseResource(
        @Schema(description = "Subscription ID", example = "1")
        Long id,

        @Schema(description = "Patient account ID", example = "1")
        Long patientAccountId,

        @Schema(description = "Active plan (FREE, TERRA, ASTRUM)", example = "TERRA")
        String plan,

        @Schema(description = "Subscription lifecycle status (ACTIVE, PENDING_PAYMENT, CANCELLED, EXPIRED)", example = "ACTIVE")
        String status,

        @Schema(description = "Expiry / renewal timestamp (null for FREE)", example = "2026-11-09T16:00:00Z")
        Instant activeUntil,

        @Schema(description = "Whether the subscription currently grants premium benefits", example = "true")
        boolean active,

        @Schema(description = "Stripe remote subscription ID if applicable", example = "sub_123456")
        String stripeSubscriptionId
) {}
