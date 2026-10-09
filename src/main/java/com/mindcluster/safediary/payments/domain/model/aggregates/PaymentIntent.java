package com.mindcluster.safediary.payments.domain.model.aggregates;

import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * PaymentIntent Aggregate Root.
 * Represents an idempotent payment operation for appointments or subscriptions.
 */
@Getter
public class PaymentIntent extends AbstractDomainAggregateRoot<PaymentIntent> {

    private Long id;
    private final String purpose;
    private final Long subjectId;
    private final Long payerAccountId;
    private final BigDecimal amount;
    private final String currency;
    private final String idempotencyKey;
    private String providerReference;
    private String status;

    public PaymentIntent(Long id,
                         String purpose,
                         Long subjectId,
                         Long payerAccountId,
                         BigDecimal amount,
                         String currency,
                         String idempotencyKey,
                         String providerReference,
                         String status) {
        this.id = id;
        this.purpose = Objects.requireNonNull(purpose, "purpose is required");
        this.subjectId = Objects.requireNonNull(subjectId, "subjectId is required");
        this.payerAccountId = Objects.requireNonNull(payerAccountId, "payerAccountId is required");
        this.amount = Objects.requireNonNull(amount, "amount is required");
        this.currency = Objects.requireNonNullElse(currency, "USD");
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey, "idempotencyKey is required");
        this.providerReference = providerReference;
        this.status = Objects.requireNonNullElse(status, "CREATED");
    }

    public PaymentIntent(String purpose,
                         Long subjectId,
                         Long payerAccountId,
                         BigDecimal amount,
                         String currency,
                         String idempotencyKey) {
        this(null, purpose, subjectId, payerAccountId, amount, currency, idempotencyKey, null, "CREATED");
    }

    public void markApproved(String providerReference) {
        this.status = "APPROVED";
        this.providerReference = providerReference;
    }

    public void markFailed() {
        this.status = "FAILED";
    }
}
