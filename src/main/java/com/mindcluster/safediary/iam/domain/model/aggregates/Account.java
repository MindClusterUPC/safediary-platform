package com.mindcluster.safediary.iam.domain.model.aggregates;

import com.mindcluster.safediary.iam.domain.model.events.IamDomainEvent;
import com.mindcluster.safediary.iam.domain.model.valueobjects.*;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * SafeDiary account. passwordHash is null when the account only signs in with Google or Apple.
 * Five wrong passwords lock the sign-in for 15 minutes.
 */
@Getter
public class Account extends AbstractDomainAggregateRoot<Account> {
    public static final int MAX_FAILED_SIGN_INS = 5;
    public static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    public static final Duration DELETION_GRACE_PERIOD = Duration.ofDays(30);
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PASSWORD_NUMBER_OR_SYMBOL = Pattern.compile(".*[^A-Za-z].*");

    private final Long id;
    private final String email;
    private String passwordHash;
    private final AccountRole role;
    private AccountStatus status;
    private boolean biometricEnabled;
    private int failedSignInAttempts;
    private Instant lockedUntil;
    private Instant deletionScheduledAt;
    private final Instant createdAt;

    public Account(Long id, String email, String passwordHash, AccountRole role, AccountStatus status, boolean biometricEnabled,
                   int failedSignInAttempts, Instant lockedUntil, Instant deletionScheduledAt, Instant createdAt) {
        this.id = id;
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        this.role = Objects.requireNonNull(role, "Role is required");
        this.status = Objects.requireNonNull(status, "Status is required");
        this.biometricEnabled = biometricEnabled;
        this.failedSignInAttempts = failedSignInAttempts;
        this.lockedUntil = lockedUntil;
        this.deletionScheduledAt = deletionScheduledAt;
        this.createdAt = createdAt;
    }

    /** Self-registration is only allowed for patients and psychologists. */
    public static Account register(String email, String passwordHash, AccountRole role) {
        if (role != AccountRole.PATIENT && role != AccountRole.PSYCHOLOGIST)
            throw new IllegalArgumentException("Only patient and psychologist accounts can be registered");
        return new Account(null, email, passwordHash, role, AccountStatus.ACTIVE, false, 0, null, null, null);
    }

    public static String normalizeEmail(String email) {
        if (email == null) throw new IllegalArgumentException("E-mail is required");
        var normalized = email.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 254 || !EMAIL.matcher(normalized).matches())
            throw new IllegalArgumentException("E-mail format is invalid");
        return normalized;
    }

    /** Password rules shown in the app: at least 8 characters and a number or special symbol. */
    public static void validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 72 || !PASSWORD_NUMBER_OR_SYMBOL.matcher(password).matches())
            throw new IllegalArgumentException("Password must have 8 to 72 characters and include a number or special symbol");
    }

    public boolean isActive() { return status == AccountStatus.ACTIVE; }
    public boolean hasPassword() { return passwordHash != null; }
    public boolean isLockedAt(Instant now) { return lockedUntil != null && now.isBefore(lockedUntil); }
    public boolean canSignInAt(Instant now) { return isActive() && !isLockedAt(now); }

    public void recordFailedSignIn(Instant now) {
        failedSignInAttempts++;
        if (failedSignInAttempts >= MAX_FAILED_SIGN_INS) {
            lockedUntil = now.plus(LOCK_DURATION);
            failedSignInAttempts = 0;
        }
    }

    public void recordSuccessfulSignIn() {
        failedSignInAttempts = 0;
        lockedUntil = null;
        registerDomainEvent(new IamDomainEvent("AccountAuthenticated", id, id));
    }

    public void changePassword(String newPasswordHash) {
        passwordHash = Objects.requireNonNull(newPasswordHash, "Password hash is required");
        failedSignInAttempts = 0;
        lockedUntil = null;
    }

    public void enableBiometricUnlock() {
        if (!isActive()) throw new IllegalStateException("Only active accounts can enable biometric unlock");
        biometricEnabled = true;
        registerDomainEvent(new IamDomainEvent("BiometricUnlockEnabled", id, id));
    }

    /** Sessions stop working immediately; data is removed after the grace period. */
    public void requestDeletion(Instant now) {
        if (status == AccountStatus.PENDING_DELETION) throw new IllegalStateException("Account deletion was already requested");
        status = AccountStatus.PENDING_DELETION;
        biometricEnabled = false;
        deletionScheduledAt = now.plus(DELETION_GRACE_PERIOD);
        registerDomainEvent(new IamDomainEvent("AccountDeletionRequested", id, id));
    }
}
