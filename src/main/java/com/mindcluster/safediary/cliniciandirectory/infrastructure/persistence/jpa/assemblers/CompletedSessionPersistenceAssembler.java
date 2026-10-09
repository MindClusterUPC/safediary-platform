package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.cliniciandirectory.domain.model.entities.CompletedSession;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.CompletedSessionPersistenceEntity;

public final class CompletedSessionPersistenceAssembler {
    private CompletedSessionPersistenceAssembler() {}
    public static CompletedSession toDomain(CompletedSessionPersistenceEntity entity) {
        return new CompletedSession(entity.getId(), entity.getAppointmentId(), entity.getClinicianId(), entity.getPatientAccountId(), entity.getDurationMinutes(), entity.getCompletedAt());
    }
    public static void copyToEntity(CompletedSession domain, CompletedSessionPersistenceEntity entity) {
        entity.setAppointmentId(domain.appointmentId());
        entity.setClinicianId(domain.clinicianId());
        entity.setPatientAccountId(domain.patientAccountId());
        entity.setDurationMinutes(domain.durationMinutes());
        entity.setCompletedAt(domain.completedAt());
    }
}
