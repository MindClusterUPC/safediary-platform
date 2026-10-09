package com.mindcluster.safediary.payments.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_intents", schema = "payments", uniqueConstraints = {
        @UniqueConstraint(name = "ux_payment_intents_idempotency_key", columnNames = {"idempotency_key"})
})
@Getter
@Setter
@NoArgsConstructor
public class PaymentIntentPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "purpose", nullable = false, length = 20)
    private String purpose;

    @Column(name = "subject_id", nullable = false)
    private Long subjectId;

    @Column(name = "payer_account_id", nullable = false)
    private Long payerAccountId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "provider_reference", length = 255)
    private String providerReference;

    @Column(name = "status", nullable = false, length = 20)
    private String status;
}
