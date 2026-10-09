package com.mindcluster.safediary.payments.domain;

import com.mindcluster.safediary.payments.domain.model.aggregates.Subscription;
import com.mindcluster.safediary.payments.domain.model.events.SubscriptionActivatedEvent;
import com.mindcluster.safediary.payments.domain.model.events.SubscriptionCancelledEvent;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionTest {

    @Test
    @DisplayName("SubscriptionPlan enum correctly specifies prices and recurrence")
    void testSubscriptionPlanProperties() {
        assertThat(SubscriptionPlan.FREE.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(SubscriptionPlan.FREE.isPaid()).isFalse();
        assertThat(SubscriptionPlan.FREE.isRecurring()).isFalse();

        assertThat(SubscriptionPlan.TERRA.getPrice()).isEqualByComparingTo(new BigDecimal("19.99"));
        assertThat(SubscriptionPlan.TERRA.isPaid()).isTrue();
        assertThat(SubscriptionPlan.TERRA.getBillingInterval()).isEqualTo("month");

        assertThat(SubscriptionPlan.ASTRUM.getPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(SubscriptionPlan.ASTRUM.isPaid()).isTrue();
        assertThat(SubscriptionPlan.ASTRUM.getBillingInterval()).isEqualTo("year");

        assertThat(SubscriptionPlan.fromString("terra")).isEqualTo(SubscriptionPlan.TERRA);
        assertThat(SubscriptionPlan.fromString("astrum")).isEqualTo(SubscriptionPlan.ASTRUM);
        assertThat(SubscriptionPlan.fromString("free")).isEqualTo(SubscriptionPlan.FREE);
    }

    @Test
    @DisplayName("createFree creates default active free subscription without end date")
    void testCreateFreeSubscription() {
        Subscription sub = Subscription.createFree(10L);

        assertThat(sub.getPatientAccountId()).isEqualTo(10L);
        assertThat(sub.getPlan()).isEqualTo(SubscriptionPlan.FREE);
        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(sub.getActiveUntil()).isNull();
        assertThat(sub.isActive()).isTrue();
    }

    @Test
    @DisplayName("Throws exception if patient account id is null or non-positive")
    void testInvalidPatientAccountId() {
        assertThatThrownBy(() -> Subscription.createFree(null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Subscription.createFree(0L))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Subscription.createFree(-5L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("activateSubscription upgrades plan, sets dates, and publishes domain event")
    void testActivateSubscription() {
        Subscription sub = Subscription.createFree(5L);
        Instant futureDate = Instant.now().plus(30, ChronoUnit.DAYS);

        sub.activateSubscription(SubscriptionPlan.TERRA, futureDate, "sub_stripe_123", "cus_stripe_abc");

        assertThat(sub.getPlan()).isEqualTo(SubscriptionPlan.TERRA);
        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(sub.getActiveUntil()).isEqualTo(futureDate);
        assertThat(sub.getStripeSubscriptionId()).isEqualTo("sub_stripe_123");
        assertThat(sub.getStripeCustomerId()).isEqualTo("cus_stripe_abc");
        assertThat(sub.isActive()).isTrue();

        assertThat(sub.domainEvents()).hasAtLeastOneElementOfType(SubscriptionActivatedEvent.class);
        SubscriptionActivatedEvent event = sub.domainEvents().stream()
                .filter(e -> e instanceof SubscriptionActivatedEvent)
                .map(e -> (SubscriptionActivatedEvent) e)
                .findFirst()
                .orElseThrow();
        assertThat(event.patientAccountId()).isEqualTo(5L);
        assertThat(event.plan()).isEqualTo(SubscriptionPlan.TERRA);
    }

    @Test
    @DisplayName("cancel sets status to CANCELLED and publishes cancellation event")
    void testCancelSubscription() {
        Subscription sub = Subscription.createFree(5L);
        sub.activateSubscription(SubscriptionPlan.ASTRUM, Instant.now().plus(365, ChronoUnit.DAYS), "sub_1", "cus_1");
        sub.clearDomainEvents();

        sub.cancel();

        assertThat(sub.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        assertThat(sub.isActive()).isFalse();
        assertThat(sub.domainEvents()).hasAtLeastOneElementOfType(SubscriptionCancelledEvent.class);
    }

    @Test
    @DisplayName("isActive returns false if activeUntil has expired")
    void testIsActiveWithExpiredDate() {
        Instant pastDate = Instant.now().minus(2, ChronoUnit.DAYS);
        Subscription sub = new Subscription(1L, SubscriptionPlan.TERRA, SubscriptionStatus.ACTIVE, pastDate, "sub_1", "cus_1");

        assertThat(sub.isActive()).isFalse();
    }
}
