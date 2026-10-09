package com.mindcluster.safediary.payments.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionStatus;
import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "subscriptions", schema = "payments", uniqueConstraints = {
        @UniqueConstraint(name = "ux_subscriptions_patient", columnNames = {"patient_account_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class SubscriptionPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "patient_account_id", nullable = false)
    private Long patientAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false, length = 20)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(name = "active_until")
    private Instant activeUntil;

    @Column(name = "stripe_subscription_id", length = 255)
    private String stripeSubscriptionId;

    @Column(name = "stripe_customer_id", length = 255)
    private String stripeCustomerId;
}
