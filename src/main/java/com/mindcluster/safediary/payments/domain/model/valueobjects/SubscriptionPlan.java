package com.mindcluster.safediary.payments.domain.model.valueobjects;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Available subscription plans in the SafeDiary platform.
 * <ul>
 *   <li><b>FREE</b>: Base free tier with emotional diary and standard features. No Stripe checkout.</li>
 *   <li><b>TERRA</b>: $19.99 USD per month recurring subscription via Stripe.</li>
 *   <li><b>ASTRUM</b>: $100.00 USD per year recurring subscription via Stripe.</li>
 * </ul>
 */
public enum SubscriptionPlan {
    FREE("Free", BigDecimal.ZERO, "USD", null),
    TERRA("Terra", new BigDecimal("19.99"), "USD", "month"),
    ASTRUM("Astrum", new BigDecimal("100.00"), "USD", "year");

    private final String displayName;
    private final BigDecimal price;
    private final String currency;
    private final String billingInterval;

    SubscriptionPlan(String displayName, BigDecimal price, String currency, String billingInterval) {
        this.displayName = displayName;
        this.price = price;
        this.currency = currency;
        this.billingInterval = billingInterval;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public String getBillingInterval() {
        return billingInterval;
    }

    public boolean isPaid() {
        return this != FREE;
    }

    public boolean isRecurring() {
        return this.billingInterval != null;
    }

    public static SubscriptionPlan fromString(String value) {
        if (value == null || value.isBlank()) {
            return FREE;
        }
        return Arrays.stream(values())
                .filter(plan -> plan.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown subscription plan: " + value));
    }
}
