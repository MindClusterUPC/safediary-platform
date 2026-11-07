package com.mindcluster.safediary.diary.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.diary.domain.model.aggregates.DiaryEntry;
import com.mindcluster.safediary.diary.domain.repositories.DiaryEntryRepository;
import com.mindcluster.safediary.diary.infrastructure.persistence.jpa.assemblers.DiaryEntryPersistenceAssembler;
import com.mindcluster.safediary.diary.infrastructure.persistence.jpa.entities.DiaryEntryPersistenceEntity;
import com.mindcluster.safediary.diary.infrastructure.persistence.jpa.repositories.DiaryEntryPersistenceRepository;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class DiaryEntryRepositoryImpl implements DiaryEntryRepository {

    private final DiaryEntryPersistenceRepository persistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public DiaryEntryRepositoryImpl(DiaryEntryPersistenceRepository persistenceRepository,
                                    DomainEventPublisher domainEventPublisher) {
        this.persistenceRepository = persistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DiaryEntry> findById(Long id) {
        return persistenceRepository.findById(id).map(DiaryEntryPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiaryEntry> findAllByPatientId(Long patientId) {
        return persistenceRepository.findAllByPatientIdOrderByEntryDateDesc(patientId).stream()
                .map(DiaryEntryPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public DiaryEntry save(DiaryEntry entry) {
        boolean isNew = entry.getId() == null;
        DiaryEntryPersistenceEntity entity = isNew
                ? new DiaryEntryPersistenceEntity()
                : persistenceRepository.findById(entry.getId())
                .orElseThrow(() -> new IllegalStateException("DiaryEntry not found: " + entry.getId()));

        DiaryEntryPersistenceAssembler.copyToEntity(entry, entity);
        DiaryEntryPersistenceEntity saved = persistenceRepository.saveAndFlush(entity);
        DiaryEntry result = DiaryEntryPersistenceAssembler.toDomain(saved);

        domainEventPublisher.publishAndClear(entry);
        return result;
    }

    @Override
    @Transactional
    public void delete(DiaryEntry entry) {
        if (entry != null && entry.getId() != null) {
            persistenceRepository.deleteById(entry.getId());
        }
    }
}
