package com.mindcluster.safediary.profiles.domain.model.valueobjects;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representing session duration and pricing.
 */
public record SessionPricing(BigDecimal costPerSession, int durationMinutes, String currency) {

    public SessionPricing {
        if (costPerSession == null || costPerSession.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Cost per session must be a non-negative number");
        }
        if (durationMinutes <= 0) {
            throw new IllegalArgumentException("Session duration must be greater than 0 minutes");
        }
        if (currency == null || currency.isBlank()) {
            currency = "PEN";
        }
    }

    public static SessionPricing of(BigDecimal cost, int durationMinutes) {
        return new SessionPricing(cost, durationMinutes, "PEN");
    }

    public static SessionPricing of(BigDecimal cost, int durationMinutes, String currency) {
        return new SessionPricing(cost, durationMinutes, currency);
    }
}
