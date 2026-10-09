package com.mindcluster.safediary.carescheduling.infrastructure.acl;

import com.mindcluster.safediary.carescheduling.application.internal.CareSchedulingException;
import com.mindcluster.safediary.carescheduling.application.internal.outboundservices.directory.*;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.cliniciandirectory.interfaces.acl.ClinicianDirectoryContextFacade;
import com.mindcluster.safediary.cliniciandirectory.interfaces.acl.dto.CompletedCareSessionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Optional;

/**
 * Anticorruption layer towards Clinician Directory (Customer/Supplier). SessionCompleted travels as the
 * CompletedCareSessionDto event already consumed by the directory CareSessionEventConsumer.
 */
@Component @RequiredArgsConstructor
public class ClinicianDirectoryAclClient implements ClinicianDirectoryClient {
    private final ClinicianDirectoryContextFacade directory;
    private final ApplicationEventPublisher events;

    public Optional<ClinicianQuote> fetchBookableClinician(Long clinicianId) {
        return directory.fetchPublishedClinicianRate(clinicianId).map(rate -> new ClinicianQuote(
                rate.clinicianId(), rate.displayName(), rate.amount(), rate.currency(), rate.durationMinutes()));
    }

    public Optional<Long> findClinicianIdByAccountId(Long accountId) {
        return directory.fetchClinicianIdByAccountId(accountId);
    }

    /** A rejection from the directory rolls back the session closure, so both contexts stay consistent. */
    public void notifySessionCompleted(Long appointmentId, Long clinicianId, Long patientAccountId, int durationMinutes, Instant completedAt) {
        try {
            events.publishEvent(new CompletedCareSessionDto(appointmentId, clinicianId, patientAccountId, durationMinutes, completedAt));
        } catch (RuntimeException ex) {
            throw new CareSchedulingException(new ApplicationError("CLINICIAN_DIRECTORY_CONFLICT",
                    "Clinician Directory rejected the completed session", ex.getMessage()));
        }
    }
}
