package com.mindcluster.safediary.iam.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.iam.domain.model.aggregates.*;
import com.mindcluster.safediary.iam.domain.model.entities.*;
import com.mindcluster.safediary.iam.infrastructure.persistence.jpa.entities.*;
import com.mindcluster.safediary.iam.infrastructure.persistence.jpa.entities.ConsentPersistenceEntity.SharingPermissionEmbeddable;

/** Translates IAM domain objects to and from their JPA persistence entities. */
public final class IamPersistenceAssembler {
    private IamPersistenceAssembler() {}

    public static Account toDomain(AccountPersistenceEntity e) {
        return new Account(e.getId(), e.getEmail(), e.getPasswordHash(), e.getRole(), e.getStatus(), e.isBiometricEnabled(),
                e.getFailedSignInAttempts(), e.getLockedUntil(), e.getDeletionScheduledAt(),
                e.getCreatedAt() == null ? null : e.getCreatedAt().toInstant());
    }
    public static void copyToEntity(Account d, AccountPersistenceEntity e) {
        e.setEmail(d.getEmail());
        e.setPasswordHash(d.getPasswordHash());
        e.setRole(d.getRole());
        e.setStatus(d.getStatus());
        e.setBiometricEnabled(d.isBiometricEnabled());
        e.setFailedSignInAttempts(d.getFailedSignInAttempts());
        e.setLockedUntil(d.getLockedUntil());
        e.setDeletionScheduledAt(d.getDeletionScheduledAt());
    }

    public static AuthProvider toDomain(AuthProviderPersistenceEntity e) {
        return new AuthProvider(e.getId(), e.getAccountId(), e.getProviderType(), e.getExternalId());
    }
    public static void copyToEntity(AuthProvider d, AuthProviderPersistenceEntity e) {
        e.setAccountId(d.accountId());
        e.setProviderType(d.type());
        e.setExternalId(d.externalId());
    }

    public static PasswordResetCode toDomain(PasswordResetCodePersistenceEntity e) {
        return new PasswordResetCode(e.getId(), e.getAccountId(), e.getCodeHash(), e.getExpiresAt(), e.getUsedAt(), e.getFailedAttempts());
    }
    public static void copyToEntity(PasswordResetCode d, PasswordResetCodePersistenceEntity e) {
        e.setAccountId(d.getAccountId());
        e.setCodeHash(d.getCodeHash());
        e.setExpiresAt(d.getExpiresAt());
        e.setUsedAt(d.getUsedAt());
        e.setFailedAttempts(d.getFailedAttempts());
    }

    public static BiometricUnlockToken toDomain(BiometricUnlockTokenPersistenceEntity e) {
        return new BiometricUnlockToken(e.getId(), e.getAccountId(), e.getTokenHash(), e.getExpiresAt(), e.getRevokedAt());
    }
    public static void copyToEntity(BiometricUnlockToken d, BiometricUnlockTokenPersistenceEntity e) {
        e.setAccountId(d.getAccountId());
        e.setTokenHash(d.getTokenHash());
        e.setExpiresAt(d.getExpiresAt());
        e.setRevokedAt(d.getRevokedAt());
    }

    public static Consent toDomain(ConsentPersistenceEntity e) {
        var permissions = e.getPermissions().stream()
                .map(p -> new SharingPermission(p.getScope(), p.getPeriodStart(), p.getPeriodEnd())).toList();
        return new Consent(e.getId(), e.getOwnerAccountId(), e.getGranteeAccountId(), e.getStatus(), permissions,
                e.getGrantedAt(), e.getExpiresAt(), e.getRevokedAt());
    }
    public static void copyToEntity(Consent d, ConsentPersistenceEntity e) {
        e.setOwnerAccountId(d.getOwnerAccountId());
        e.setGranteeAccountId(d.getGranteeAccountId());
        e.setStatus(d.getStatus());
        e.setGrantedAt(d.getGrantedAt());
        e.setExpiresAt(d.getExpiresAt());
        e.setRevokedAt(d.getRevokedAt());
        e.getPermissions().clear();
        d.getPermissions().forEach(p -> {
            var embeddable = new SharingPermissionEmbeddable();
            embeddable.setScope(p.scope());
            embeddable.setPeriodStart(p.periodStart());
            embeddable.setPeriodEnd(p.periodEnd());
            e.getPermissions().add(embeddable);
        });
    }

    public static PrivateVault toDomain(PrivateVaultPersistenceEntity e) {
        return new PrivateVault(e.getId(), e.getAccountId(), e.getPinHash(), e.getDiaryEntryIds(), e.getFailedAttempts(), e.getLockedUntil());
    }
    public static void copyToEntity(PrivateVault d, PrivateVaultPersistenceEntity e) {
        e.setAccountId(d.getAccountId());
        e.setPinHash(d.getPinHash());
        e.setFailedAttempts(d.getFailedAttempts());
        e.setLockedUntil(d.getLockedUntil());
        e.getDiaryEntryIds().clear();
        e.getDiaryEntryIds().addAll(d.getProtectedEntryIds());
    }

    public static AuditEvent toDomain(AuditEventPersistenceEntity e) {
        return new AuditEvent(e.getId(), e.getActorAccountId(), e.getTargetAccountId(), e.getConsentId(), e.getAction(),
                e.getSourceContext(), e.getResourceType(), e.getResourceId(), e.getOccurredAt());
    }
    public static void copyToEntity(AuditEvent d, AuditEventPersistenceEntity e) {
        e.setActorAccountId(d.actorAccountId());
        e.setTargetAccountId(d.targetAccountId());
        e.setConsentId(d.consentId());
        e.setAction(d.action());
        e.setSourceContext(d.sourceContext());
        e.setResourceType(d.resourceType());
        e.setResourceId(d.resourceId());
        e.setOccurredAt(d.occurredAt());
    }
}
