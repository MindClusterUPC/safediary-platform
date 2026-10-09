package com.mindcluster.safediary.carescheduling.interfaces.rest.transform;

import com.mindcluster.safediary.carescheduling.application.commandservices.dto.AuthorizedSummaryDto;
import com.mindcluster.safediary.carescheduling.domain.model.aggregates.*;
import com.mindcluster.safediary.carescheduling.domain.model.entities.*;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import com.mindcluster.safediary.carescheduling.interfaces.rest.resources.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public final class CareSchedulingResourceAssembler {
    private CareSchedulingResourceAssembler() {}

    public static CoordinationMessageResource message(CoordinationMessage m) {
        return new CoordinationMessageResource(m.id(), m.contactRequestId(), m.senderAccountId(), m.body(), m.sentAt());
    }
    public static ContactRequestResource contactRequest(ContactRequest r, CoordinationMessage lastMessage) {
        return new ContactRequestResource(r.getId(), r.getPatientAccountId(), r.getClinicianId(), r.getStatus(),
                r.getCreatedAt(), lastMessage == null ? null : message(lastMessage));
    }
    public static AppointmentResource appointment(Appointment a) {
        var now = Instant.now();
        var hold = a.getHold();
        var status = a.isHoldExpiredAt(now) ? AppointmentStatus.EXPIRED : a.getStatus();
        long remaining = hold != null && hold.isActiveAt(now) ? Duration.between(now, hold.getExpiresAt()).toSeconds() : 0;
        var window = a.accessWindow();
        return new AppointmentResource(a.getId(), a.getContactRequestId(), a.getPatientAccountId(), a.getClinicianId(),
                a.getSlot().startsAt(), a.getSlot().endsAt(), a.getSlot().timezone(), a.getAmount().amount(),
                a.getAmount().currency(), status, a.getPaymentReference(), hold == null ? null : hold.getExpiresAt(),
                remaining, window.opensAt(), window.closesAt());
    }
    public static List<AppointmentResource> appointments(List<Appointment> values) {
        return values.stream().map(CareSchedulingResourceAssembler::appointment).toList();
    }
    public static AvailabilityWindowResource availability(AvailabilitySlot s) {
        return new AvailabilityWindowResource(s.window().dayOfWeek(), s.window().startTime(), s.window().endTime());
    }
    public static AvailabilityWindow window(AvailabilityWindowResource r) {
        return new AvailabilityWindow(r.dayOfWeek(), r.startTime(), r.endTime());
    }
    public static BookableSlotResource slot(BookableSlot s) {
        return new BookableSlotResource(s.startsAt(), s.endsAt(), s.availability());
    }
    public static ClinicalSessionResource session(ClinicalSession s, boolean withJoinUrl) {
        return new ClinicalSessionResource(s.getId(), s.getAppointmentId(), s.getStatus(), s.getStartedAt(), s.getEndedAt(),
                s.attendedMinutes(), withJoinUrl ? s.getProviderRoomRef() : null);
    }
    public static AuthorizedSummaryResource summary(AuthorizedSummaryDto d) {
        var s = d.summary();
        return new AuthorizedSummaryResource(d.appointmentId(), d.patientAccountId(), d.consentRef(), d.accessedAt(),
                s.periodStart(), s.periodEnd(), s.dominantEmotions(), s.keyTriggers(), s.synthesisNarrative(), s.highlights());
    }
}
