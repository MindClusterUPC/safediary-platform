package com.mindcluster.safediary.iam.interfaces.rest.transform;

import com.mindcluster.safediary.iam.application.commandservices.dto.*;
import com.mindcluster.safediary.iam.domain.model.aggregates.*;
import com.mindcluster.safediary.iam.domain.model.entities.SharingPermission;
import com.mindcluster.safediary.iam.interfaces.rest.resources.*;
import java.time.Instant;
import java.util.List;

public final class IamResourceAssembler {
    private IamResourceAssembler() {}

    public static AuthenticatedAccountResource session(AuthenticatedAccountDto d) {
        var a = d.account();
        return new AuthenticatedAccountResource(a.getId(), a.getEmail(), a.getRole(), d.accessToken(), "Bearer", d.expiresAt());
    }
    public static AccountResource account(Account a) {
        return new AccountResource(a.getId(), a.getEmail(), a.getRole(), a.getStatus(), a.isBiometricEnabled(),
                a.getCreatedAt(), a.getDeletionScheduledAt());
    }
    public static BiometricUnlockResource biometric(BiometricUnlockDto d) {
        return new BiometricUnlockResource(d.unlockToken(), d.expiresAt());
    }
    public static SharingPermission permission(SharingPermissionResource r) {
        return new SharingPermission(r.scope(), r.periodStart(), r.periodEnd());
    }
    public static ConsentScopeResource consent(Consent c) {
        var permissions = c.getPermissions().stream()
                .map(p -> new SharingPermissionResource(p.scope(), p.periodStart(), p.periodEnd())).toList();
        return new ConsentScopeResource(c.getId(), c.getOwnerAccountId(), c.getGranteeAccountId(), c.statusAt(Instant.now()),
                permissions, c.getGrantedAt(), c.getExpiresAt(), c.getRevokedAt());
    }
    public static List<ConsentScopeResource> consents(List<Consent> values) {
        return values.stream().map(IamResourceAssembler::consent).toList();
    }
    public static PrivateVaultResource vault(PrivateVault v) { return new PrivateVaultResource(v.getId(), v.getProtectedEntryIds()); }
    public static AuditEventResource audit(AuditEvent e) {
        return new AuditEventResource(e.id(), e.actorAccountId(), e.consentId(), e.action(), e.sourceContext(),
                e.resourceType(), e.resourceId(), e.occurredAt());
    }
}
