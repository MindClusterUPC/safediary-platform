package com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.carescheduling.domain.model.aggregates.*;
import com.mindcluster.safediary.carescheduling.domain.model.entities.*;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.entities.*;
import java.time.DayOfWeek;

/** Translates Care Scheduling domain objects to and from their JPA persistence entities. */
public final class CareSchedulingPersistenceAssembler {
    private CareSchedulingPersistenceAssembler() {}

    public static ContactRequest toDomain(ContactRequestPersistenceEntity e) {
        return new ContactRequest(e.getId(), e.getPatientAccountId(), e.getClinicianId(), e.getStatus(),
                e.getCreatedAt() == null ? null : e.getCreatedAt().toInstant());
    }
    public static void copyToEntity(ContactRequest d, ContactRequestPersistenceEntity e) {
        e.setPatientAccountId(d.getPatientAccountId());
        e.setClinicianId(d.getClinicianId());
        e.setStatus(d.getStatus());
    }

    public static CoordinationMessage toDomain(CoordinationMessagePersistenceEntity e) {
        return new CoordinationMessage(e.getId(), e.getContactRequestId(), e.getSenderAccountId(), e.getBody(), e.getSentAt());
    }
    public static void copyToEntity(CoordinationMessage d, CoordinationMessagePersistenceEntity e) {
        e.setContactRequestId(d.contactRequestId());
        e.setSenderAccountId(d.senderAccountId());
        e.setBody(d.body());
        e.setSentAt(d.sentAt());
    }

    public static AvailabilitySlot toDomain(AvailabilitySlotPersistenceEntity e) {
        return new AvailabilitySlot(e.getId(), e.getClinicianId(),
                new AvailabilityWindow(DayOfWeek.of(e.getDayOfWeek()), e.getStartTime(), e.getEndTime()), e.isActive());
    }
    public static void copyToEntity(AvailabilitySlot d, AvailabilitySlotPersistenceEntity e) {
        e.setClinicianId(d.clinicianId());
        e.setDayOfWeek((short) d.window().dayOfWeek().getValue());
        e.setStartTime(d.window().startTime());
        e.setEndTime(d.window().endTime());
        e.setActive(d.active());
    }

    public static Appointment toDomain(AppointmentPersistenceEntity e, SlotHoldPersistenceEntity hold) {
        return new Appointment(e.getId(), e.getContactRequestId(), e.getPatientAccountId(), e.getClinicianId(),
                new AppointmentSlot(e.getStartsAt(), e.getEndsAt(), e.getTimezone()),
                new AgreedAmount(e.getAgreedAmount(), e.getCurrency()), e.getStatus(), e.getPaymentReference(),
                hold == null ? null : new SlotHold(hold.getId(), hold.getExpiresAt(), hold.getStatus()));
    }
    public static void copyToEntity(Appointment d, AppointmentPersistenceEntity e) {
        e.setContactRequestId(d.getContactRequestId());
        e.setPatientAccountId(d.getPatientAccountId());
        e.setClinicianId(d.getClinicianId());
        e.setStartsAt(d.getSlot().startsAt());
        e.setEndsAt(d.getSlot().endsAt());
        e.setTimezone(d.getSlot().timezone());
        e.setAgreedAmount(d.getAmount().amount());
        e.setCurrency(d.getAmount().currency());
        e.setStatus(d.getStatus());
        e.setPaymentReference(d.getPaymentReference());
    }
    public static void copyToEntity(Long appointmentId, SlotHold d, SlotHoldPersistenceEntity e) {
        e.setAppointmentId(appointmentId);
        e.setExpiresAt(d.getExpiresAt());
        e.setStatus(d.getStatus());
    }

    public static ClinicalSession toDomain(ClinicalSessionPersistenceEntity e) {
        return new ClinicalSession(e.getId(), e.getAppointmentId(), e.getProviderRoomRef(), e.getStartedAt(), e.getEndedAt(), e.getStatus());
    }
    public static void copyToEntity(ClinicalSession d, ClinicalSessionPersistenceEntity e) {
        e.setAppointmentId(d.getAppointmentId());
        e.setProviderRoomRef(d.getProviderRoomRef());
        e.setStartedAt(d.getStartedAt());
        e.setEndedAt(d.getEndedAt());
        e.setStatus(d.getStatus());
    }

    public static SummaryAccessAudit toDomain(SummaryAccessAuditPersistenceEntity e) {
        return new SummaryAccessAudit(e.getId(), e.getAppointmentId(), e.getClinicianAccountId(), e.getConsentRef(), e.getAccessedAt());
    }
    public static void copyToEntity(SummaryAccessAudit d, SummaryAccessAuditPersistenceEntity e) {
        e.setAppointmentId(d.appointmentId());
        e.setClinicianAccountId(d.clinicianAccountId());
        e.setConsentRef(d.consentRef());
        e.setAccessedAt(d.accessedAt());
    }
}
