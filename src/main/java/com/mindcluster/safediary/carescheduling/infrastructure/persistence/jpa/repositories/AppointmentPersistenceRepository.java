package com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.repositories;

import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.entities.*;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppointmentPersistenceRepository extends JpaRepository<AppointmentPersistenceEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AppointmentPersistenceEntity a where a.id = :id")
    Optional<AppointmentPersistenceEntity> findByIdForUpdate(@Param("id") Long id);
    List<AppointmentPersistenceEntity> findByPatientAccountId(Long patientAccountId);
    List<AppointmentPersistenceEntity> findByClinicianId(Long clinicianId);
    @Query("select a from AppointmentPersistenceEntity a where a.clinicianId = :clinicianId and a.startsAt < :to and a.endsAt > :from")
    List<AppointmentPersistenceEntity> findByClinicianIdOverlapping(@Param("clinicianId") Long clinicianId,
                                                                    @Param("from") Instant from, @Param("to") Instant to);
    List<AppointmentPersistenceEntity> findByContactRequestIdAndStatus(Long contactRequestId, AppointmentStatus status);
    @Query("""
            select a from AppointmentPersistenceEntity a, SlotHoldPersistenceEntity h
            where h.appointmentId = a.id and a.status = com.mindcluster.safediary.carescheduling.domain.model.valueobjects.AppointmentStatus.HELD
              and h.status = com.mindcluster.safediary.carescheduling.domain.model.valueobjects.SlotHoldStatus.ACTIVE
              and h.expiresAt <= :now""")
    List<AppointmentPersistenceEntity> findHeldWithHoldExpiredAt(@Param("now") Instant now);
}
