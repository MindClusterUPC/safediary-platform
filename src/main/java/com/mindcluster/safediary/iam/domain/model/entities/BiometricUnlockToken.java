package com.mindcluster.safediary.iam.domain.model.entities;

import lombok.Getter;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Long-lived device credential released by the phone only after a successful FaceID or fingerprint check.
 * The server stores its SHA-256 hash, never the raw token.
 */
@Getter
public class BiometricUnlockToken {
    public static final Duration VALIDITY = Duration.ofDays(90);

    private final Long id;
    private final Long accountId;
    private final String tokenHash;
    private final Instant expiresAt;
    private Instant revokedAt;

    public BiometricUnlockToken(Long id, Long accountId, String tokenHash, Instant expiresAt, Instant revokedAt) {
        this.id = id;
        this.accountId = Objects.requireNonNull(accountId, "Account is required");
        this.tokenHash = Objects.requireNonNull(tokenHash, "Token hash is required");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiration is required");
        this.revokedAt = revokedAt;
    }

    public static BiometricUnlockToken issue(Long accountId, String tokenHash, Instant now) {
        return new BiometricUnlockToken(null, accountId, tokenHash, now.plus(VALIDITY), null);
    }

    public boolean isUsableAt(Instant now) { return revokedAt == null && now.isBefore(expiresAt); }
    public void revoke(Instant now) { if (revokedAt == null) revokedAt = now; }
}
