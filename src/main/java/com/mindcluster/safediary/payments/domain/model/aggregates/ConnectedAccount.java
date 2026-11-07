package com.mindcluster.safediary.payments.domain.model.aggregates;

import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * ConnectedAccount Aggregate Root.
 * Manages Stripe Connect Custom/Express accounts for psychologists in the marketplace,
 * tracking onboarding completion, charges capability, and payout permissions.
 */
@Getter
public class ConnectedAccount extends AbstractDomainAggregateRoot<ConnectedAccount> {

    private Long id;
    private final Long psychologistAccountId;
    private final String stripeAccountId;
    private boolean chargesEnabled;
    private boolean payoutsEnabled;
    private boolean detailsSubmitted;
    private final Instant createdAt;
    private Instant updatedAt;

    public ConnectedAccount(Long id,
                            Long psychologistAccountId,
                            String stripeAccountId,
                            boolean chargesEnabled,
                            boolean payoutsEnabled,
                            boolean detailsSubmitted,
                            Instant createdAt,
                            Instant updatedAt) {
        this.id = id;
        this.psychologistAccountId = Objects.requireNonNull(psychologistAccountId, "psychologistAccountId is required");
        this.stripeAccountId = Objects.requireNonNull(stripeAccountId, "stripeAccountId is required");
        this.chargesEnabled = chargesEnabled;
        this.payoutsEnabled = payoutsEnabled;
        this.detailsSubmitted = detailsSubmitted;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public ConnectedAccount(Long psychologistAccountId, String stripeAccountId) {
        this(null, psychologistAccountId, stripeAccountId, false, false, false, Instant.now(), Instant.now());
    }

    public void updateStatus(boolean chargesEnabled, boolean payoutsEnabled, boolean detailsSubmitted) {
        this.chargesEnabled = chargesEnabled;
        this.payoutsEnabled = payoutsEnabled;
        this.detailsSubmitted = detailsSubmitted;
        this.updatedAt = Instant.now();
    }

    public boolean isReady() {
        return this.chargesEnabled && this.payoutsEnabled;
    }
}
