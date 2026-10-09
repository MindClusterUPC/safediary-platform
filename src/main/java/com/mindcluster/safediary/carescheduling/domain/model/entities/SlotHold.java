package com.mindcluster.safediary.carescheduling.domain.model.entities;

import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.SlotHoldStatus;
import lombok.Getter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/** Temporary one-hour lock of the agreed slot while the patient pays. Owned by the Appointment aggregate. */
@Getter
public class SlotHold {
    private final Long id;
    private final Instant expiresAt;
    private SlotHoldStatus status;

    public SlotHold(Long id, Instant expiresAt, SlotHoldStatus status) {
        this.id = id;
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiration is required").truncatedTo(ChronoUnit.SECONDS);
        this.status = Objects.requireNonNull(status, "Status is required");
    }
    public static SlotHold start(Instant now, java.time.Duration duration) {
        return new SlotHold(null, now.plus(duration), SlotHoldStatus.ACTIVE);
    }
    public boolean isActiveAt(Instant moment) { return status == SlotHoldStatus.ACTIVE && moment.isBefore(expiresAt); }
    public boolean isExpiredAt(Instant moment) { return status == SlotHoldStatus.ACTIVE && !moment.isBefore(expiresAt); }
    public void expire() { requireActive(); status = SlotHoldStatus.EXPIRED; }
    public void release() { requireActive(); status = SlotHoldStatus.RELEASED; }
    public void confirm() { requireActive(); status = SlotHoldStatus.CONFIRMED; }
    private void requireActive() {
        if (status != SlotHoldStatus.ACTIVE) throw new IllegalStateException("The slot hold is no longer active");
    }
}
