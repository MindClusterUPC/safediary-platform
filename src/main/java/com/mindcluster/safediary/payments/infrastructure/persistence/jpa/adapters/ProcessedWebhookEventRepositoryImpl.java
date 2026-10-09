package com.mindcluster.safediary.payments.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.payments.domain.model.entities.ProcessedWebhookEvent;
import com.mindcluster.safediary.payments.domain.repositories.ProcessedWebhookEventRepository;
import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.assemblers.ProcessedWebhookEventPersistenceAssembler;
import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.entities.ProcessedWebhookEventPersistenceEntity;
import com.mindcluster.safediary.payments.infrastructure.persistence.jpa.repositories.ProcessedWebhookEventPersistenceRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class ProcessedWebhookEventRepositoryImpl implements ProcessedWebhookEventRepository {

    private final ProcessedWebhookEventPersistenceRepository persistenceRepository;

    public ProcessedWebhookEventRepositoryImpl(ProcessedWebhookEventPersistenceRepository persistenceRepository) {
        this.persistenceRepository = persistenceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEventId(String eventId) {
        return persistenceRepository.existsByEventId(eventId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProcessedWebhookEvent> findByEventId(String eventId) {
        return persistenceRepository.findByEventId(eventId)
                .map(ProcessedWebhookEventPersistenceAssembler::toDomain);
    }

    @Override
    @Transactional
    public ProcessedWebhookEvent save(ProcessedWebhookEvent event) {
        ProcessedWebhookEventPersistenceEntity entity = ProcessedWebhookEventPersistenceAssembler.toEntity(event);
        ProcessedWebhookEventPersistenceEntity saved = persistenceRepository.saveAndFlush(entity);
        return ProcessedWebhookEventPersistenceAssembler.toDomain(saved);
    }
}
