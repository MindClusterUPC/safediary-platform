package com.mindcluster.safediary.payments.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentHistoryItemResource(
        String id,
        String title,
        String description,
        BigDecimal amount,
        String amountFormatted,
        String currency,
        String referenceNumber,
        String category,
        String initials,
        Instant createdAt
) {}
