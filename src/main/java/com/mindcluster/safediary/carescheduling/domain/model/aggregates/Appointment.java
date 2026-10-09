package com.mindcluster.safediary.carescheduling.domain.model.aggregates;

import com.mindcluster.safediary.carescheduling.domain.model.entities.SlotHold;
import com.mindcluster.safediary.carescheduling.domain.model.events.CareSchedulingDomainEvent;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Agreed schedule between a patient and a psychologist. While REQUESTED it is only a proposal; HELD keeps the slot
 * for one hour while the patient pays; only an approved payment for a live hold makes it CONFIRMED.
 */
@Getter
public class Appointment extends AbstractDomainAggregateRoot<Appointment> {
    public static final Duration HOLD_DURATION = Duration.ofHours(1);

    private final Long id;
    private final Long contactRequestId;
    private final Long patientAccountId;
    private final Long clinicianId;
    private final AppointmentSlot slot;
    private final AgreedAmount amount;
    private AppointmentStatus status;
    private String paymentReference;
    private SlotHold hold;

    public Appointment(Long id, Long contactRequestId, Long patientAccountId, Long clinicianId, AppointmentSlot slot,
                       AgreedAmount amount, AppointmentStatus status, String paymentReference, SlotHold hold) {
        if (patientAccountId == null || patientAccountId <= 0 || clinicianId == null || clinicianId <= 0)
            throw new IllegalArgumentException("An appointment requires a patient and a clinician");
        this.id = id;
        this.contactRequestId = contactRequestId;
        this.patientAccountId = patientAccountId;
        this.clinicianId = clinicianId;
        this.slot = Objects.requireNonNull(slot, "Slot is required");
        this.amount = Objects.requireNonNull(amount, "Agreed amount is required");
        this.status = Objects.requireNonNull(status, "Status is required");
        this.paymentReference = paymentReference;
        this.hold = hold;
    }

    public static Appointment propose(ContactRequest request, AppointmentSlot slot, AgreedAmount amount, Instant now) {
        if (!request.isOpen()) throw new IllegalStateException("The contact request is no longer open");
        if (!slot.startsAt().isAfter(now.plus(HOLD_DURATION)))
            throw new IllegalArgumentException("A proposal must start after the one-hour payment hold");
        return new Appointment(null, request.getId(), request.getPatientAccountId(), request.getClinicianId(), slot,
                amount, AppointmentStatus.REQUESTED, null, null);
    }

    public boolean hasParticipant(CareActor participant) {
        return participant.participatesIn(patientAccountId, clinicianId);
    }

    /** True while the appointment occupies the clinician slot. */
    public boolean occupiesSlotAt(Instant moment) {
        return status == AppointmentStatus.CONFIRMED || (status == AppointmentStatus.HELD && hold != null && hold.isActiveAt(moment));
    }

    public boolean isHoldExpiredAt(Instant moment) {
        return status == AppointmentStatus.HELD && hold != null && hold.isExpiredAt(moment);
    }

    public MeetingAccessWindow accessWindow() { return MeetingAccessWindow.of(slot); }

    /** The patient accepts the proposal: the slot is held for one hour without confirming the appointment. */
    public void acceptAndHold(Instant now) {
        if (status != AppointmentStatus.REQUESTED) throw new IllegalStateException("Only a pending proposal can be accepted");
        if (!slot.startsAt().isAfter(now)) throw new IllegalStateException("The proposed schedule has already passed");
        hold = SlotHold.start(now, HOLD_DURATION);
        status = AppointmentStatus.HELD;
        registerDomainEvent(new CareSchedulingDomainEvent("ScheduleAccepted", contactRequestId, id));
        registerDomainEvent(new CareSchedulingDomainEvent("SlotHeld", contactRequestId, id));
    }

    public void attachPaymentReference(String reference, Instant now) {
        if (status != AppointmentStatus.HELD || hold == null || !hold.isActiveAt(now))
            throw new IllegalStateException("Payment can only be requested while the slot hold is active");
        if (reference == null || reference.isBlank() || reference.length() > 128)
            throw new IllegalArgumentException("Payment reference is invalid");
        paymentReference = reference;
    }

    public void expireHold(Instant now) {
        if (!isHoldExpiredAt(now)) throw new IllegalStateException("The slot hold has not expired");
        hold.expire();
        status = AppointmentStatus.EXPIRED;
        registerDomainEvent(new CareSchedulingDomainEvent("HoldExpired", contactRequestId, id));
    }

    /** Applies an approved payment. Callers must check idempotency and late payments before calling it. */
    public void confirmPayment(String reference, BigDecimal paidAmount, String paidCurrency, Instant now) {
        if (status != AppointmentStatus.HELD || hold == null || !hold.isActiveAt(now))
            throw new IllegalStateException("Only an active slot hold can be confirmed");
        if (!amount.matches(paidAmount, paidCurrency))
            throw new IllegalArgumentException("The approved payment does not match the agreed amount");
        if (reference == null || reference.isBlank() || reference.length() > 128)
            throw new IllegalArgumentException("Payment reference is invalid");
        hold.confirm();
        paymentReference = reference;
        status = AppointmentStatus.CONFIRMED;
        registerDomainEvent(new CareSchedulingDomainEvent("AppointmentConfirmed", contactRequestId, id));
    }

    /** Cancels a proposal, a hold or a confirmed appointment and releases the slot. */
    public void cancel() {
        switch (status) {
            case REQUESTED, CONFIRMED -> status = AppointmentStatus.CANCELLED;
            case HELD -> { if (hold != null && hold.getStatus() == SlotHoldStatus.ACTIVE) hold.release(); status = AppointmentStatus.CANCELLED; }
            default -> throw new IllegalStateException("The appointment can no longer be cancelled");
        }
        registerDomainEvent(new CareSchedulingDomainEvent("AppointmentCancelled", contactRequestId, id));
    }

    public void complete() {
        if (status != AppointmentStatus.CONFIRMED) throw new IllegalStateException("Only confirmed appointments can be completed");
        status = AppointmentStatus.COMPLETED;
    }

    /** A confirmed appointment that did not take place is closed without being marked as completed. */
    public void markNotHeld() {
        if (status != AppointmentStatus.CONFIRMED) throw new IllegalStateException("Only confirmed appointments can be closed");
        status = AppointmentStatus.CANCELLED;
        registerDomainEvent(new CareSchedulingDomainEvent("AppointmentNotHeld", contactRequestId, id));
    }
}
