package com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

public record ConsultationRate(BigDecimal amount, String currency, int durationMinutes, int version) {
    public ConsultationRate {
        if (amount == null || amount.signum() < 0 || amount.scale() > 2 || amount.precision() > 10)
            throw new IllegalArgumentException("Amount must be non-negative with at most two decimal places");
        if (currency == null) throw new IllegalArgumentException("Currency is required");
        currency = currency.trim().toUpperCase(Locale.ROOT);
        Currency.getInstance(currency);
        if (durationMinutes < 1 || durationMinutes > 480 || version < 1)
            throw new IllegalArgumentException("Duration must be between 1 and 480 minutes and version positive");
    }
}
