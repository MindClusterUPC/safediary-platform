package com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.carescheduling.domain.model.aggregates.ContactRequest;
import com.mindcluster.safediary.carescheduling.domain.model.entities.CoordinationMessage;
import com.mindcluster.safediary.carescheduling.domain.model.valueobjects.ContactRequestStatus;
import com.mindcluster.safediary.carescheduling.domain.repositories.ContactRequestRepository;
import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.assemblers.CareSchedulingPersistenceAssembler;
import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.entities.*;
import com.mindcluster.safediary.carescheduling.infrastructure.persistence.jpa.repositories.*;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository @RequiredArgsConstructor @Transactional(readOnly=true)
public class ContactRequestRepositoryImpl implements ContactRequestRepository {
    private static final List<ContactRequestStatus> OPEN = List.of(ContactRequestStatus.PENDING, ContactRequestStatus.ACCEPTED);
    private final ContactRequestPersistenceRepository persistence;
    private final CoordinationMessagePersistenceRepository messages;
    private final DomainEventPublisher events;

    public Optional<ContactRequest> findById(Long id) { return persistence.findById(id).map(CareSchedulingPersistenceAssembler::toDomain); }
    @Transactional public Optional<ContactRequest> findByIdForUpdate(Long id) { return persistence.findByIdForUpdate(id).map(CareSchedulingPersistenceAssembler::toDomain); }
    public Optional<ContactRequest> findOpenByPatientAndClinician(Long patientAccountId, Long clinicianId) {
        return persistence.findByPatientAccountIdAndClinicianIdAndStatusInOrderByIdDesc(patientAccountId, clinicianId, OPEN)
                .stream().findFirst().map(CareSchedulingPersistenceAssembler::toDomain);
    }
    public List<ContactRequest> findByPatientAccountId(Long id) { return persistence.findByPatientAccountId(id).stream().map(CareSchedulingPersistenceAssembler::toDomain).toList(); }
    public List<ContactRequest> findByClinicianId(Long id) { return persistence.findByClinicianId(id).stream().map(CareSchedulingPersistenceAssembler::toDomain).toList(); }

    @Transactional public ContactRequest save(ContactRequest value) {
        var entity = value.getId() == null ? new ContactRequestPersistenceEntity() : persistence.findById(value.getId()).orElseThrow();
        CareSchedulingPersistenceAssembler.copyToEntity(value, entity);
        var saved = persistence.saveAndFlush(entity);
        events.publishAndClear(value);
        return CareSchedulingPersistenceAssembler.toDomain(saved);
    }

    public List<CoordinationMessage> findMessages(Long contactRequestId) {
        return messages.findByContactRequestIdOrderBySentAtAscIdAsc(contactRequestId).stream()
                .map(CareSchedulingPersistenceAssembler::toDomain).toList();
    }
    @Transactional public CoordinationMessage saveMessage(CoordinationMessage message) {
        var entity = new CoordinationMessagePersistenceEntity();
        CareSchedulingPersistenceAssembler.copyToEntity(message, entity);
        return CareSchedulingPersistenceAssembler.toDomain(messages.saveAndFlush(entity));
    }
}
