package com.mindcluster.safediary.carescheduling.domain.repositories;

import com.mindcluster.safediary.carescheduling.domain.model.aggregates.ContactRequest;
import com.mindcluster.safediary.carescheduling.domain.model.entities.CoordinationMessage;
import java.util.List;
import java.util.Optional;

/** Persists contact requests and their coordination messages. */
public interface ContactRequestRepository {
    Optional<ContactRequest> findById(Long id);
    Optional<ContactRequest> findByIdForUpdate(Long id);
    Optional<ContactRequest> findOpenByPatientAndClinician(Long patientAccountId, Long clinicianId);
    List<ContactRequest> findByPatientAccountId(Long patientAccountId);
    List<ContactRequest> findByClinicianId(Long clinicianId);
    ContactRequest save(ContactRequest value);
    List<CoordinationMessage> findMessages(Long contactRequestId);
    CoordinationMessage saveMessage(CoordinationMessage message);
}
