package com.mindcluster.safediary.iam.domain;

import com.mindcluster.safediary.iam.domain.model.aggregates.Account;
import com.mindcluster.safediary.iam.domain.model.valueobjects.*;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.*;

class AccountTest {
    private static final Instant NOW = Instant.parse("2026-10-10T15:00:00Z");

    @Test void onlyPatientsAndPsychologistsSelfRegisterWithANormalizedEmail() {
        var account = Account.register("  Maria@SafePlace.com ", "hash", AccountRole.PATIENT);
        assertThat(account.getEmail()).isEqualTo("maria@safeplace.com");
        assertThat(account.isActive()).isTrue();
        assertThatThrownBy(() -> Account.register("admin@safeplace.com", "hash", AccountRole.ADMIN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Account.register("not-an-email", "hash", AccountRole.PATIENT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test void passwordNeedsEightCharactersAndANumberOrSymbol() {
        assertThatCode(() -> Account.validatePassword("SafeSanctuary2025#")).doesNotThrowAnyException();
        assertThatThrownBy(() -> Account.validatePassword("short1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Account.validatePassword("onlyletters")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test void fiveFailedSignInsLockTheAccountForFifteenMinutes() {
        var account = Account.register("maria@safeplace.com", "hash", AccountRole.PATIENT);
        for (int i = 0; i < 4; i++) account.recordFailedSignIn(NOW);
        assertThat(account.canSignInAt(NOW)).isTrue();
        account.recordFailedSignIn(NOW);
        assertThat(account.canSignInAt(NOW.plusSeconds(60))).isFalse();
        assertThat(account.canSignInAt(NOW.plus(Account.LOCK_DURATION))).isTrue();
    }

    @Test void deletionRequestEndsAccessAndSchedulesTheRemoval() {
        var account = Account.register("maria@safeplace.com", "hash", AccountRole.PATIENT);
        account.enableBiometricUnlock();
        account.requestDeletion(NOW);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.PENDING_DELETION);
        assertThat(account.isBiometricEnabled()).isFalse();
        assertThat(account.getDeletionScheduledAt()).isEqualTo(NOW.plus(Account.DELETION_GRACE_PERIOD));
        assertThat(account.canSignInAt(NOW)).isFalse();
        assertThatThrownBy(() -> account.requestDeletion(NOW)).isInstanceOf(IllegalStateException.class);
    }
}
