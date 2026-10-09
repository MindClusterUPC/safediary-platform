package com.mindcluster.safediary.cliniciandirectory.domain.repositories;

import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.CompletedSession;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import java.util.List;
import java.util.Optional;

public interface CompletedSessionRepository {
    Optional<CompletedSession> findByAppointmentId(Long id);
    List<CompletedSession> findByClinicianId(Long id);
    CompletedSession save(CompletedSession value);
}
