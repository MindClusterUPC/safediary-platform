package com.mindcluster.safediary.payments.application.commandservices.dto;

import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;

/**
 * Result of creating a subscription checkout.
 */
public record SubscriptionCheckoutResultDto(
        Long patientAccountId,
        SubscriptionPlan plan,
        String checkoutUrl,
        String sessionId,
        boolean checkoutRequired,
        String message
) {}
