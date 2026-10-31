package com.mindcluster.safediary.payments.infrastructure.persistence.jpa.entities;

import com.mindcluster.safediary.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "connected_accounts", schema = "payments", uniqueConstraints = {
        @UniqueConstraint(name = "ux_connected_accounts_psychologist", columnNames = {"psychologist_account_id"}),
        @UniqueConstraint(name = "ux_connected_accounts_stripe_account", columnNames = {"stripe_account_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class ConnectedAccountPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "psychologist_account_id", nullable = false)
    private Long psychologistAccountId;

    @Column(name = "stripe_account_id", nullable = false, length = 120)
    private String stripeAccountId;

    @Column(name = "charges_enabled", nullable = false)
    private boolean chargesEnabled;

    @Column(name = "payouts_enabled", nullable = false)
    private boolean payoutsEnabled;

    @Column(name = "details_submitted", nullable = false)
    private boolean detailsSubmitted;
}
