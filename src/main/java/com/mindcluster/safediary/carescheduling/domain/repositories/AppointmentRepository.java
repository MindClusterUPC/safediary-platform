package com.mindcluster.safediary.carescheduling.domain.repositories;

import com.mindcluster.safediary.carescheduling.domain.model.aggregates.Appointment;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.AppointmentStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {
    Optional<Appointment> findById(Long id);
    Optional<Appointment> findByIdForUpdate(Long id);
    List<Appointment> findByPatientAccountId(Long patientAccountId);
    List<Appointment> findByClinicianId(Long clinicianId);
    /** Appointments of a clinician whose slot overlaps the interval, in any status. */
    List<Appointment> findByClinicianIdOverlapping(Long clinicianId, Instant from, Instant to);
    List<Appointment> findByContactRequestIdAndStatus(Long contactRequestId, AppointmentStatus status);
    List<Appointment> findHeldWithHoldExpiredAt(Instant now);
    Appointment save(Appointment value);
}
