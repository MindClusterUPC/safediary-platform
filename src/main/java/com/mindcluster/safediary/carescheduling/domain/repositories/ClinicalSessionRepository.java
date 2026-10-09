package com.mindcluster.safediary.carescheduling.domain.repositories;

import com.mindcluster.safediary.carescheduling.domain.model.aggregates.ClinicalSession;
import java.util.Optional;

public interface ClinicalSessionRepository {
    Optional<ClinicalSession> findByAppointmentId(Long appointmentId);
    ClinicalSession save(ClinicalSession value);
}
