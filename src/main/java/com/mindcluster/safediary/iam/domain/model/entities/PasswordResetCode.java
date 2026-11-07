package com.mindcluster.safediary.iam.domain.model.entities;

import lombok.Getter;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** Single-use 6-digit recovery code sent by e-mail. Only its hash is stored. */
@Getter
public class PasswordResetCode {
    public static final Duration VALIDITY = Duration.ofMinutes(10);
    public static final int MAX_ATTEMPTS = 5;

    private final Long id;
    private final Long accountId;
    private final String codeHash;
    private final Instant expiresAt;
    private Instant usedAt;
    private int failedAttempts;

    public PasswordResetCode(Long id, Long accountId, String codeHash, Instant expiresAt, Instant usedAt, int failedAttempts) {
        this.id = id;
        this.accountId = Objects.requireNonNull(accountId, "Account is required");
        this.codeHash = Objects.requireNonNull(codeHash, "Code hash is required");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiration is required");
        this.usedAt = usedAt;
        this.failedAttempts = failedAttempts;
    }

    public static PasswordResetCode issue(Long accountId, String codeHash, Instant now) {
        return new PasswordResetCode(null, accountId, codeHash, now.plus(VALIDITY), null, 0);
    }

    public boolean isUsableAt(Instant now) {
        return usedAt == null && now.isBefore(expiresAt) && failedAttempts < MAX_ATTEMPTS;
    }
    public void recordFailedAttempt() { failedAttempts++; }
    public void markUsed(Instant now) { usedAt = now; }
    public void invalidate(Instant now) { if (usedAt == null) usedAt = now; }
}
