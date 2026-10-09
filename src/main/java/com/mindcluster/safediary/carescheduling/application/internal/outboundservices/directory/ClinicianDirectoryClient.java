package com.mindcluster.safediary.carescheduling.application.internal.outboundservices.directory;

import java.time.Instant;
import java.util.Optional;

public interface ClinicianDirectoryClient {
    /** Only clinicians that are verified and published can be contacted or booked. */
    Optional<ClinicianQuote> fetchBookableClinician(Long clinicianId);

    Optional<Long> findClinicianIdByAccountId(Long accountId);

    /** Publishes SessionCompleted so the directory can enable one review for this appointment. */
    void notifySessionCompleted(Long appointmentId, Long clinicianId, Long patientAccountId, int durationMinutes, Instant completedAt);
}
