package com.mindcluster.safediary.iam.domain.model.aggregates;

import com.mindcluster.safediary.iam.domain.model.entities.SharingPermission;
import com.mindcluster.safediary.iam.domain.model.events.IamDomainEvent;
import com.mindcluster.safediary.iam.domain.model.valueobjects.*;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Explicit, revocable authorization from a patient (owner) to a specialist (grantee) over part of the history. */
@Getter
public class Consent extends AbstractDomainAggregateRoot<Consent> {
    private final Long id;
    private final Long ownerAccountId;
    private final Long granteeAccountId;
    private ConsentStatus status;
    private final List<SharingPermission> permissions;
    private final Instant grantedAt;
    private final Instant expiresAt;
    private Instant revokedAt;

    public Consent(Long id, Long ownerAccountId, Long granteeAccountId, ConsentStatus status, List<SharingPermission> permissions,
                   Instant grantedAt, Instant expiresAt, Instant revokedAt) {
        if (ownerAccountId == null || granteeAccountId == null || ownerAccountId.equals(granteeAccountId))
            throw new IllegalArgumentException("A consent requires two different accounts");
        if (permissions == null || permissions.isEmpty())
            throw new IllegalArgumentException("Select at least one kind of information to share");
        if (permissions.stream().map(SharingPermission::scope).distinct().count() != permissions.size())
            throw new IllegalArgumentException("Each kind of information can only be shared once per consent");
        this.id = id;
        this.ownerAccountId = ownerAccountId;
        this.granteeAccountId = granteeAccountId;
        this.status = Objects.requireNonNull(status, "Status is required");
        this.permissions = List.copyOf(permissions);
        this.grantedAt = Objects.requireNonNull(grantedAt, "Grant date is required");
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public static Consent grant(Long ownerAccountId, Long granteeAccountId, List<SharingPermission> permissions,
                                Instant expiresAt, Instant now) {
        if (expiresAt != null && !expiresAt.isAfter(now)) throw new IllegalArgumentException("The consent must expire in the future");
        return new Consent(null, ownerAccountId, granteeAccountId, ConsentStatus.ACTIVE, permissions, now, expiresAt, null);
    }

    /** Status taking the validity into account, even before the expiration is persisted. */
    public ConsentStatus statusAt(Instant now) {
        return status == ConsentStatus.ACTIVE && expiresAt != null && !now.isBefore(expiresAt) ? ConsentStatus.EXPIRED : status;
    }
    public boolean isActiveAt(Instant now) { return statusAt(now) == ConsentStatus.ACTIVE; }

    public boolean covers(SharingScope scope, LocalDate date, Instant now) {
        return isActiveAt(now) && permissions.stream().anyMatch(p -> p.covers(scope, date));
    }

    public void revoke(Instant now) {
        if (!isActiveAt(now)) throw new IllegalStateException("Only an active consent can be revoked");
        status = ConsentStatus.REVOKED;
        revokedAt = now;
        registerDomainEvent(new IamDomainEvent("ConsentRevoked", ownerAccountId, id));
    }

    /** Persists the expiration once its validity ended; returns true when the status changed. */
    public boolean expireIfDue(Instant now) {
        if (status != ConsentStatus.ACTIVE || statusAt(now) != ConsentStatus.EXPIRED) return false;
        status = ConsentStatus.EXPIRED;
        return true;
    }
}
