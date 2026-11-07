package com.mindcluster.safediary.iam.domain.model.aggregates;

import com.mindcluster.safediary.iam.domain.model.events.IamDomainEvent;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Protected space unlocked with a secondary PIN. It only keeps references to diary entries, never their content.
 * Five wrong PINs block the vault for 15 minutes.
 */
@Getter
public class PrivateVault extends AbstractDomainAggregateRoot<PrivateVault> {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final Pattern PIN = Pattern.compile("^\\d{4,8}$");

    private final Long id;
    private final Long accountId;
    private final String pinHash;
    private final Set<Long> protectedEntryIds;
    private int failedAttempts;
    private Instant lockedUntil;

    public PrivateVault(Long id, Long accountId, String pinHash, Set<Long> protectedEntryIds, int failedAttempts, Instant lockedUntil) {
        this.id = id;
        this.accountId = Objects.requireNonNull(accountId, "Account is required");
        this.pinHash = Objects.requireNonNull(pinHash, "PIN hash is required");
        this.protectedEntryIds = new LinkedHashSet<>(protectedEntryIds == null ? Set.of() : protectedEntryIds);
        this.failedAttempts = failedAttempts;
        this.lockedUntil = lockedUntil;
    }

    public static PrivateVault create(Long accountId, String pinHash) {
        return new PrivateVault(null, accountId, pinHash, Set.of(), 0, null);
    }

    public static void validatePin(String pin) {
        if (pin == null || !PIN.matcher(pin).matches()) throw new IllegalArgumentException("The vault PIN must have 4 to 8 digits");
    }

    public Set<Long> getProtectedEntryIds() { return Set.copyOf(protectedEntryIds); }
    public boolean isLockedAt(Instant now) { return lockedUntil != null && now.isBefore(lockedUntil); }

    public void recordFailedAttempt(Instant now) {
        failedAttempts++;
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            lockedUntil = now.plus(LOCK_DURATION);
            failedAttempts = 0;
        }
    }
    public void recordSuccessfulAccess() {
        failedAttempts = 0;
        lockedUntil = null;
    }

    public void protect(Long diaryEntryId) {
        if (diaryEntryId == null || diaryEntryId <= 0) throw new IllegalArgumentException("Diary entry ID must be positive");
        if (protectedEntryIds.add(diaryEntryId))
            registerDomainEvent(new IamDomainEvent("VaultEntryProtected", accountId, diaryEntryId));
    }
    public boolean protects(Long diaryEntryId) { return protectedEntryIds.contains(diaryEntryId); }
}
