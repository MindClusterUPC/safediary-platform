package com.mindcluster.safediary.carescheduling.domain.model.valueobjects;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

/** Price captured from Clinician Directory when the schedule is proposed; later rate changes do not alter it. */
public record AgreedAmount(BigDecimal amount, String currency) {
    public AgreedAmount {
        if (amount == null || amount.signum() < 0 || amount.scale() > 2 || amount.precision() > 10)
            throw new IllegalArgumentException("Amount must be non-negative with at most two decimal places");
        if (currency == null) throw new IllegalArgumentException("Currency is required");
        currency = currency.trim().toUpperCase(Locale.ROOT);
        Currency.getInstance(currency);
    }
    public boolean matches(BigDecimal otherAmount, String otherCurrency) {
        return otherAmount != null && otherCurrency != null && amount.compareTo(otherAmount) == 0
                && currency.equalsIgnoreCase(otherCurrency.trim());
    }
}
