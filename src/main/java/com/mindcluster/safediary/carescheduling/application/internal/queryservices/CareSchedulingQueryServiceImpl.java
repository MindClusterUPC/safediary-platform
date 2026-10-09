package com.mindcluster.safediary.carescheduling.application.internal.queryservices;

import com.mindcluster.safediary.carescheduling.application.internal.CareSchedulingPolicy;
import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.directory.ClinicianDirectoryClient;
import com.mindcluster.safediary.carescheduling.application.queryservices.CareSchedulingQueryService;
import com.mindcluster.safediary.carescheduling.domain.model.aggregates.*;
import com.mindcluster.safediary.carescheduling.domain.model.entities.*;
import com.mindcluster.safediary.carescheduling.domain.model.queries.*;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import com.mindcluster.safediary.carescheduling.domain.repositories.*;
import com.mindcluster.safediary.carescheduling.domain.services.BookableSlotCalculator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import static com.mindcluster.safediary.carescheduling.application.internal.CareSchedulingPolicy.*;

@Service @Transactional(readOnly=true)
public class CareSchedulingQueryServiceImpl implements CareSchedulingQueryService {
    private static final Duration MAX_AGENDA_RANGE = Duration.ofDays(62);
    private final ContactRequestRepository requests;
    private final AppointmentRepository appointments;
    private final AvailabilitySlotRepository availability;
    private final ClinicalSessionRepository sessions;
    private final ClinicianDirectoryClient directory;
    private final CareSchedulingPolicy policy;
    private final ZoneId zone;
    private final int slotStepMinutes;

    public CareSchedulingQueryServiceImpl(ContactRequestRepository requests, AppointmentRepository appointments,
            AvailabilitySlotRepository availability, ClinicalSessionRepository sessions, ClinicianDirectoryClient directory,
            CareSchedulingPolicy policy, @Value("${care-scheduling.zone:America/Lima}") String zoneId,
            @Value("${care-scheduling.slot-step-minutes:60}") int slotStepMinutes) {
        this.requests = requests; this.appointments = appointments; this.availability = availability;
        this.sessions = sessions; this.directory = directory; this.policy = policy;
        this.zone = ZoneId.of(zoneId); this.slotStepMinutes = slotStepMinutes;
    }

    public List<ContactRequest> handle(GetContactRequestsQuery q) {
        var participant = policy.participant(q.actor());
        var found = participant.isPatient() ? requests.findByPatientAccountId(participant.accountId())
                : requests.findByClinicianId(participant.clinicianId());
        return found.stream().sorted(Comparator.comparing(ContactRequest::getId).reversed()).toList();
    }

    public List<CoordinationMessage> handle(GetContactRequestQuery q) {
        var request = requests.findById(q.contactRequestId()).orElseThrow(() -> missing("ContactRequest", q.contactRequestId()));
        if (!request.hasParticipant(policy.participant(q.actor())))
            throw forbidden("Only the patient and the psychologist can access this chat");
        return requests.findMessages(request.getId());
    }

    public List<Appointment> handle(GetPatientAppointmentsQuery q) {
        var patient = policy.patient(q.actor());
        return appointments.findByPatientAccountId(patient.accountId()).stream()
                .sorted(Comparator.comparing((Appointment a) -> a.getSlot().startsAt()).reversed()).toList();
    }

    public List<Appointment> handle(GetClinicianAgendaQuery q) {
        var psychologist = policy.psychologist(q.actor());
        var from = q.from() != null ? q.from() : LocalDate.now(zone).atStartOfDay(zone).toInstant();
        var to = q.to() != null ? q.to() : from.plus(Duration.ofDays(7));
        if (!to.isAfter(from) || Duration.between(from, to).compareTo(MAX_AGENDA_RANGE) > 0)
            throw new IllegalArgumentException("The agenda range must be positive and at most 62 days");
        return appointments.findByClinicianIdOverlapping(psychologist.clinicianId(), from, to).stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED && a.getStatus() != AppointmentStatus.EXPIRED)
                .sorted(Comparator.comparing(a -> a.getSlot().startsAt())).toList();
    }

    /** The agenda owner sees held and confirmed slots; other users only see whether a slot is free. */
    public List<BookableSlot> handle(GetBookableSlotsQuery q) {
        var participant = policy.participant(q.actor());
        if (q.clinicianId() == null || q.clinicianId() <= 0 || q.date() == null)
            throw new IllegalArgumentException("Clinician and date are required");
        var quote = directory.fetchBookableClinician(q.clinicianId()).orElseThrow(() -> missing("Clinician", q.clinicianId()));
        var dayStart = q.date().atStartOfDay(zone).toInstant();
        var dayEnd = q.date().plusDays(1).atStartOfDay(zone).toInstant();
        var windows = availability.findActiveByClinicianId(q.clinicianId()).stream().map(AvailabilitySlot::window).toList();
        boolean owner = participant.isPsychologist() && participant.clinicianId().equals(q.clinicianId());
        return BookableSlotCalculator.calculate(q.date(), zone, windows, quote.durationMinutes(), slotStepMinutes,
                appointments.findByClinicianIdOverlapping(q.clinicianId(), dayStart, dayEnd), Instant.now(), owner);
    }

    public Optional<ClinicalSession> handle(GetSessionDetailsQuery q) {
        var appointment = appointments.findById(q.appointmentId()).orElseThrow(() -> missing("Appointment", q.appointmentId()));
        if (!appointment.hasParticipant(policy.participant(q.actor())))
            throw forbidden("Only the participants of this appointment can access it");
        return sessions.findByAppointmentId(appointment.getId());
    }

    public Optional<CoordinationMessage> latestMessage(Long contactRequestId) {
        var messages = requests.findMessages(contactRequestId);
        return messages.isEmpty() ? Optional.empty() : Optional.of(messages.getLast());
    }
}
