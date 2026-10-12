package com.mindcluster.safediary.iam.domain;

import com.mindcluster.safediary.iam.domain.model.aggregates.Consent;
import com.mindcluster.safediary.iam.domain.model.entities.SharingPermission;
import com.mindcluster.safediary.iam.domain.model.valueobjects.*;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class ConsentTest {
    private static final Instant NOW = Instant.parse("2026-10-10T15:00:00Z");
    private static final List<SharingPermission> SUMMARIES_OF_OCTOBER = List.of(new SharingPermission(
            SharingScope.AI_CLINICAL_SUMMARIES, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)));

    @Test void coversOnlyTheAuthorizedScopeAndPeriod() {
        var consent = Consent.grant(1L, 2L, SUMMARIES_OF_OCTOBER, null, NOW);
        assertThat(consent.covers(SharingScope.AI_CLINICAL_SUMMARIES, LocalDate.of(2026, 10, 9), NOW)).isTrue();
        assertThat(consent.covers(SharingScope.AI_CLINICAL_SUMMARIES, LocalDate.of(2026, 11, 2), NOW)).isFalse();
        assertThat(consent.covers(SharingScope.DIARY_ENTRIES, LocalDate.of(2026, 10, 9), NOW)).isFalse();
    }

    @Test void revokedOrExpiredConsentsStopCoveringImmediately() {
        var revoked = Consent.grant(1L, 2L, SUMMARIES_OF_OCTOBER, null, NOW);
        revoked.revoke(NOW);
        assertThat(revoked.getStatus()).isEqualTo(ConsentStatus.REVOKED);
        assertThat(revoked.covers(SharingScope.AI_CLINICAL_SUMMARIES, null, NOW)).isFalse();
        assertThatThrownBy(() -> revoked.revoke(NOW)).isInstanceOf(IllegalStateException.class);

        var expiring = Consent.grant(1L, 2L, SUMMARIES_OF_OCTOBER, NOW.plus(Duration.ofDays(7)), NOW);
        var later = NOW.plus(Duration.ofDays(8));
        assertThat(expiring.statusAt(later)).isEqualTo(ConsentStatus.EXPIRED);
        assertThat(expiring.expireIfDue(later)).isTrue();
        assertThat(expiring.getStatus()).isEqualTo(ConsentStatus.EXPIRED);
    }

    @Test void rejectsSelfConsentEmptyScopeAndDuplicatedScopes() {
        assertThatThrownBy(() -> Consent.grant(1L, 1L, SUMMARIES_OF_OCTOBER, null, NOW)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Consent.grant(1L, 2L, List.of(), null, NOW)).isInstanceOf(IllegalArgumentException.class);
        var duplicated = List.of(new SharingPermission(SharingScope.DIARY_ENTRIES, null, null),
                new SharingPermission(SharingScope.DIARY_ENTRIES, null, null));
        assertThatThrownBy(() -> Consent.grant(1L, 2L, duplicated, null, NOW)).isInstanceOf(IllegalArgumentException.class);
    }
}
