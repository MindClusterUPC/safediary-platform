package com.mindcluster.safediary.iam.domain.model.events;

import java.time.Instant;

/**
 * Notification with identifiers only (AccountRegistered, AccountAuthenticated, BiometricUnlockEnabled,
 * PasswordResetRequested, AccountDeletionRequested, ConsentGranted, ConsentRevoked, VaultEntryProtected, AccessAudited).
 * Credentials, codes and tokens never travel in events.
 */
public record IamDomainEvent(String type, Long accountId, Long resourceId, Instant occurredAt) {
    public IamDomainEvent(String type, Long accountId, Long resourceId) {
        this(type, accountId, resourceId, Instant.now());
    }
}
