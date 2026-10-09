package com.mindcluster.safediary.carescheduling.application.internal.commandservices;

import com.mindcluster.safediary.carescheduling.application.commandservices.CareSchedulingCommandService;
import com.mindcluster.safediary.carescheduling.application.commandservices.dto.AuthorizedSummaryDto;
import com.mindcluster.safediary.carescheduling.application.internal.CareSchedulingPolicy;
import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.directory.ClinicianDirectoryClient;
import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.iam.IamClient;
import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.payments.PaymentsClient;
import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.summaries.EmotionalSummaryClient;
import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.video.VideoProviderClient;
import com.mindcluster.safediary.carescheduling.domain.model.aggregates.*;
import com.mindcluster.safediary.carescheduling.domain.model.commands.*;
import com.mindcluster.safediary.carescheduling.domain.model.entities.*;
import com.mindcluster.safediary.carescheduling.domain.model.events.CareSchedulingDomainEvent;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import com.mindcluster.safediary.carescheduling.domain.repositories.*;
import com.mindcluster.safediary.shared.application.result.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import static com.mindcluster.safediary.carescheduling.application.internal.CareSchedulingPolicy.*;

/**
 * Handlers of section 2.6.7.3: contact requests, schedule agreement, slot hold, payment result, cancellation,
 * hold expiration, clinical session and authorized summary. Locks are always taken in the same order
 * (contact request, clinician agenda, appointment) so concurrent proposals and acceptances cannot deadlock.
 */
@Service @Transactional
public class CareSchedulingCommandServiceImpl implements CareSchedulingCommandService {
    private static final int MAX_WINDOWS = 50;
    private final ContactRequestRepository requests;
    private final AvailabilitySlotRepository availability;
    private final AppointmentRepository appointments;
    private final ClinicalSessionRepository sessions;
    private final SummaryAccessAuditRepository audits;
    private final ClinicianDirectoryClient directory;
    private final PaymentsClient payments;
    private final VideoProviderClient video;
    private final IamClient iam;
    private final EmotionalSummaryClient summaries;
    private final CareSchedulingPolicy policy;
    private final ApplicationEventPublisher events;
    private final ZoneId zone;

    public CareSchedulingCommandServiceImpl(ContactRequestRepository requests, AvailabilitySlotRepository availability,
            AppointmentRepository appointments, ClinicalSessionRepository sessions, SummaryAccessAuditRepository audits,
            ClinicianDirectoryClient directory, PaymentsClient payments, VideoProviderClient video, IamClient iam,
            EmotionalSummaryClient summaries, CareSchedulingPolicy policy, ApplicationEventPublisher events,
            @Value("${care-scheduling.zone:America/Lima}") String zoneId) {
        this.requests = requests; this.availability = availability; this.appointments = appointments;
        this.sessions = sessions; this.audits = audits; this.directory = directory; this.payments = payments;
        this.video = video; this.iam = iam; this.summaries = summaries; this.policy = policy; this.events = events;
        this.zone = ZoneId.of(zoneId);
    }

    private ContactRequest lockedRequest(Long id, CareActor participant) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Contact request ID must be positive");
        var request = requests.findByIdForUpdate(id).orElseThrow(() -> missing("ContactRequest", id));
        if (!request.hasParticipant(participant)) throw forbidden("Only the patient and the psychologist can access this chat");
        return request;
    }
    private Appointment lockedAppointment(Long id, CareActor participant) {
        if (id == null || id <= 0) throw new IllegalArgumentException("Appointment ID must be positive");
        var appointment = appointments.findByIdForUpdate(id).orElseThrow(() -> missing("Appointment", id));
        if (participant != null && !appointment.hasParticipant(participant))
            throw forbidden("Only the participants of this appointment can access it");
        return appointment;
    }
    private ClinicalSession sessionOf(Appointment appointment) {
        return sessions.findByAppointmentId(appointment.getId()).orElseThrow(() -> missing("ClinicalSession", appointment.getId()));
    }
    /** Expires stale holds that overlap the slot and reports whether another reservation still occupies it. */
    private boolean occupied(Long clinicianId, AppointmentSlot slot, Long ignoredId, Instant now) {
        boolean occupied = false;
        for (var other : appointments.findByClinicianIdOverlapping(clinicianId, slot.startsAt(), slot.endsAt())) {
            if (other.getId().equals(ignoredId)) continue;
            if (other.isHoldExpiredAt(now)) { other.expireHold(now); appointments.save(other); }
            else if (other.occupiesSlotAt(now)) occupied = true;
        }
        return occupied;
    }

    // ContactRequestHandler: opens the chat with a published clinician or reuses the open one.
    public Result<ContactRequest, ApplicationError> handle(SendContactRequestCommand c) {
        return attempt(() -> {
            var patient = policy.patient(c.actor());
            if (c.clinicianId() == null || c.clinicianId() <= 0) throw new IllegalArgumentException("Clinician ID must be positive");
            directory.fetchBookableClinician(c.clinicianId()).orElseThrow(() ->
                    rule("clinician-eligibility", "Only verified and published clinicians can be contacted"));
            var request = requests.findOpenByPatientAndClinician(patient.accountId(), c.clinicianId()).orElseGet(() -> {
                var created = requests.save(ContactRequest.open(patient.accountId(), c.clinicianId()));
                events.publishEvent(new CareSchedulingDomainEvent("ContactRequestSent", created.getId(), null));
                return created;
            });
            requests.saveMessage(request.sendMessage(patient, c.message(), Instant.now()));
            return request;
        });
    }

    public Result<ContactRequest, ApplicationError> handle(RespondToContactRequestCommand c) {
        return attempt(() -> {
            var psychologist = policy.psychologist(c.actor());
            var request = lockedRequest(c.contactRequestId(), psychologist);
            if (c.accept()) request.accept(); else request.reject();
            return requests.save(request);
        });
    }

    public Result<CoordinationMessage, ApplicationError> handle(SendCoordinationMessageCommand c) {
        return attempt(() -> {
            var participant = policy.participant(c.actor());
            var request = lockedRequest(c.contactRequestId(), participant);
            var message = request.sendMessage(participant, c.body(), Instant.now());
            requests.save(request);
            return requests.saveMessage(message);
        });
    }

    public Result<List<AvailabilitySlot>, ApplicationError> handle(SetWeeklyAvailabilityCommand c) {
        return attempt(() -> {
            var psychologist = policy.psychologist(c.actor());
            var windows = c.windows();
            if (windows == null || windows.size() > MAX_WINDOWS || windows.stream().anyMatch(Objects::isNull))
                throw new IllegalArgumentException("Send between 0 and 50 availability windows");
            for (int i = 0; i < windows.size(); i++)
                for (int j = i + 1; j < windows.size(); j++)
                    if (windows.get(i).overlaps(windows.get(j))) throw new IllegalArgumentException("Availability windows must not overlap");
            availability.lockActiveByClinicianId(psychologist.clinicianId());
            return availability.replaceActive(psychologist.clinicianId(), windows);
        });
    }

    // ScheduleAgreementHandler: proposes a free slot and captures the directory rate; no hold and no charge yet.
    public Result<Appointment, ApplicationError> handle(ProposeScheduleCommand c) {
        return attempt(() -> {
            var psychologist = policy.psychologist(c.actor());
            if (c.startsAt() == null) throw new IllegalArgumentException("Start time is required");
            var request = lockedRequest(c.contactRequestId(), psychologist);
            if (!request.isOpen()) throw conflict("The contact request is no longer open");
            var quote = directory.fetchBookableClinician(request.getClinicianId()).orElseThrow(() ->
                    rule("clinician-eligibility", "Only verified and published clinicians can propose schedules"));
            var now = Instant.now();
            var slot = new AppointmentSlot(c.startsAt(), c.startsAt().plusSeconds(quote.durationMinutes() * 60L), zone.getId());
            var windows = availability.lockActiveByClinicianId(request.getClinicianId());
            var start = slot.startsAt().atZone(zone);
            var end = slot.endsAt().atZone(zone);
            boolean insideAvailability = start.toLocalDate().equals(end.toLocalDate()) && windows.stream()
                    .anyMatch(w -> w.window().dayOfWeek() == start.getDayOfWeek()
                            && w.window().contains(start.toLocalTime(), end.toLocalTime()));
            if (!insideAvailability) throw rule("availability", "The proposed schedule is outside the clinician availability");
            if (occupied(request.getClinicianId(), slot, null, now)) throw conflict("The proposed schedule is already occupied");
            if (!appointments.findByContactRequestIdAndStatus(request.getId(), AppointmentStatus.HELD).isEmpty())
                throw conflict("A reserved slot of this chat is still waiting for payment");
            var proposal = Appointment.propose(request, slot, new AgreedAmount(quote.amount(), quote.currency()), now);
            request.accept();
            requests.save(request);
            appointments.findByContactRequestIdAndStatus(request.getId(), AppointmentStatus.REQUESTED)
                    .forEach(previous -> { previous.cancel(); appointments.save(previous); });
            var saved = appointments.save(proposal);
            events.publishEvent(new CareSchedulingDomainEvent("ScheduleProposed", request.getId(), saved.getId()));
            return saved;
        });
    }

    // HoldSlotHandler: the patient accepts, the slot is held for one hour and the charge is requested to Payments.
    public Result<Appointment, ApplicationError> handle(AcceptScheduleCommand c) {
        return attempt(() -> {
            var patient = policy.patient(c.actor());
            if (c.appointmentId() == null || c.appointmentId() <= 0) throw new IllegalArgumentException("Proposal ID must be positive");
            var preview = appointments.findById(c.appointmentId()).orElseThrow(() -> missing("Appointment", c.appointmentId()));
            if (!preview.hasParticipant(patient)) throw forbidden("Only the patient of this proposal can accept it");
            if (availability.lockActiveByClinicianId(preview.getClinicianId()).isEmpty())
                throw rule("availability", "The clinician no longer publishes availability");
            var appointment = lockedAppointment(c.appointmentId(), patient);
            var now = Instant.now();
            if (appointment.getStatus() == AppointmentStatus.HELD && appointment.getHold().isActiveAt(now)) return appointment;
            if (appointment.getStatus() != AppointmentStatus.REQUESTED) throw conflict("The proposal is no longer available");
            var request = requests.findById(appointment.getContactRequestId()).orElseThrow(() ->
                    missing("ContactRequest", appointment.getContactRequestId()));
            if (!request.isOpen()) throw conflict("The contact request is no longer open");
            directory.fetchBookableClinician(appointment.getClinicianId()).orElseThrow(() ->
                    rule("clinician-eligibility", "The clinician is no longer available in the directory"));
            if (occupied(appointment.getClinicianId(), appointment.getSlot(), appointment.getId(), now))
                throw conflict("The schedule is no longer available; agree on a new one in the chat");
            appointment.acceptAndHold(now);
            appointment.attachPaymentReference(payments.requestAppointmentCharge(appointment), now);
            return appointments.save(appointment);
        });
    }

    /**
     * PaymentResultHandler: applies the verified result published by Payments and Payouts. Duplicated notifications
     * never confirm a second appointment; an approved payment after the hold was released is sent back for refund.
     * A failed payment keeps the hold so the patient can retry while it is still active.
     */
    public Result<Appointment, ApplicationError> handle(ConfirmAfterPaymentCommand c) {
        return attempt(() -> {
            var appointment = lockedAppointment(c.appointmentId(), null);
            if (c.paymentReference() == null || !c.paymentReference().equals(appointment.getPaymentReference()))
                throw new IllegalArgumentException("The payment reference does not belong to this reservation");
            var now = Instant.now();
            if (!c.approved()) {
                events.publishEvent(new CareSchedulingDomainEvent("PaymentFailedForHold", appointment.getContactRequestId(), appointment.getId()));
                return appointment;
            }
            if (appointment.getStatus() == AppointmentStatus.CONFIRMED || appointment.getStatus() == AppointmentStatus.COMPLETED)
                return appointment;
            if (appointment.isHoldExpiredAt(now)) appointment.expireHold(now);
            if (appointment.getStatus() != AppointmentStatus.HELD) {
                var released = appointments.save(appointment);
                payments.requestRefund(released, "LATE_PAYMENT");
                return released;
            }
            appointment.confirmPayment(c.paymentReference(), c.amount(), c.currency(), now);
            var confirmed = appointments.save(appointment);
            sessions.save(ClinicalSession.schedule(confirmed, video.createRoom(confirmed.getId())));
            return confirmed;
        });
    }

    /** Releases the slot; an already paid appointment is reported to Payments and Payouts to apply its refund policy. */
    public Result<Appointment, ApplicationError> handle(CancelAppointmentCommand c) {
        return attempt(() -> {
            var participant = policy.participant(c.actor());
            var appointment = lockedAppointment(c.appointmentId(), participant);
            var session = sessions.findByAppointmentId(appointment.getId());
            if (session.isPresent() && session.get().getStatus() == ClinicalSessionStatus.IN_PROGRESS)
                throw conflict("The session already started; close it from the session instead");
            boolean paid = appointment.getStatus() == AppointmentStatus.CONFIRMED;
            appointment.cancel();
            var saved = appointments.save(appointment);
            session.ifPresent(s -> { s.cancel(); sessions.save(s); });
            if (paid) payments.requestRefund(saved, participant.isPatient() ? "CANCELLED_BY_PATIENT" : "CANCELLED_BY_CLINICIAN");
            return saved;
        });
    }

    // ExpireHoldHandler
    public int handle(ExpireSlotHoldsCommand c) {
        int expired = 0;
        for (var candidate : appointments.findHeldWithHoldExpiredAt(c.now())) {
            var appointment = lockedAppointment(candidate.getId(), null);
            if (!appointment.isHoldExpiredAt(c.now())) continue;
            appointment.expireHold(c.now());
            appointments.save(appointment);
            expired++;
        }
        return expired;
    }

    // SessionHandler: only the two participants enter, only while the appointment is confirmed and its window is open.
    public Result<ClinicalSession, ApplicationError> handle(JoinSessionCommand c) {
        return attempt(() -> {
            var appointment = lockedAppointment(c.appointmentId(), policy.participant(c.actor()));
            if (appointment.getStatus() != AppointmentStatus.CONFIRMED)
                throw rule("meeting-access", "Only confirmed appointments give access to the meeting");
            var window = appointment.accessWindow();
            if (!window.isOpenAt(Instant.now()))
                throw rule("meeting-access", "The meeting can be joined from %s to %s".formatted(
                        DateTimeFormatter.ISO_INSTANT.format(window.opensAt()), DateTimeFormatter.ISO_INSTANT.format(window.closesAt())));
            var session = sessionOf(appointment);
            session.join(Instant.now());
            return sessions.save(session);
        });
    }

    // SessionHandler + SessionCompletedPublisher: the psychologist records the operational outcome, no clinical content.
    public Result<ClinicalSession, ApplicationError> handle(CompleteSessionCommand c) {
        return attempt(() -> {
            var appointment = lockedAppointment(c.appointmentId(), policy.psychologist(c.actor()));
            if (c.outcome() == null) throw new IllegalArgumentException("Outcome is required");
            if (appointment.getStatus() != AppointmentStatus.CONFIRMED) throw conflict("Only confirmed appointments can be closed");
            var now = Instant.now();
            if (now.isBefore(appointment.accessWindow().opensAt()))
                throw rule("session-closure", "The attention can be closed once its access window opens");
            var session = sessionOf(appointment);
            session.close(c.outcome(), now);
            var saved = sessions.save(session);
            if (c.outcome() == SessionOutcome.COMPLETED) {
                appointment.complete();
                appointments.save(appointment);
                directory.notifySessionCompleted(appointment.getId(), appointment.getClinicianId(),
                        appointment.getPatientAccountId(), saved.attendedMinutes(), saved.getEndedAt());
            } else {
                appointment.markNotHeld();
                payments.requestRefund(appointments.save(appointment),
                        c.outcome() == SessionOutcome.NO_SHOW ? "PATIENT_NO_SHOW" : "SESSION_NOT_HELD");
            }
            return saved;
        });
    }

    // AuthorizedSummaryHandler: validates the current consent in IAM before reading the minimal summary from AssistantAI.
    public Result<AuthorizedSummaryDto, ApplicationError> handle(RequestAuthorizedSummaryCommand c) {
        return attempt(() -> {
            var psychologist = policy.psychologist(c.actor());
            var appointment = lockedAppointment(c.appointmentId(), psychologist);
            if (appointment.getStatus() != AppointmentStatus.CONFIRMED && appointment.getStatus() != AppointmentStatus.COMPLETED)
                throw rule("authorized-summary", "Summaries are only available for confirmed appointments");
            var consentRef = iam.findActiveSummaryConsent(appointment.getPatientAccountId(), psychologist.accountId())
                    .orElseThrow(() -> forbidden("The patient has not granted an active consent to share an emotional summary"));
            var summary = summaries.fetchLatestSummary(appointment.getPatientAccountId())
                    .orElseThrow(() -> missing("EmotionalSummary", appointment.getId()));
            var audit = audits.save(new SummaryAccessAudit(null, appointment.getId(), psychologist.accountId(), consentRef, Instant.now()));
            return new AuthorizedSummaryDto(appointment.getId(), appointment.getPatientAccountId(), consentRef, audit.accessedAt(), summary);
        });
    }
}
