package com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.adapters;

import com.mindcluster.safediary.cliniciandirectory.domain.model.aggregates.Review;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import com.mindcluster.safediary.cliniciandirectory.domain.repositories.ReviewRepository;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.entities.ReviewPersistenceEntity;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.repositories.ReviewPersistenceRepository;
import com.mindcluster.safediary.cliniciandirectory.infrastructure.persistence.jpa.assemblers.ReviewPersistenceAssembler;
import com.mindcluster.safediary.shared.infrastructure.events.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository @RequiredArgsConstructor @Transactional(readOnly=true)
public class ReviewRepositoryImpl implements ReviewRepository {
    public Optional<Long> findClinicianIdById(Long id) { return persistence.findClinicianIdById(id); }
    private final ReviewPersistenceRepository persistence;
    private final DomainEventPublisher events;
    public Optional<Review> findById(Long id) { return persistence.findById(id).map(ReviewPersistenceAssembler::toDomain); }
    public Optional<Review> findByAppointmentId(Long id) { return persistence.findByAppointmentId(id).map(ReviewPersistenceAssembler::toDomain); }
    public List<Review> findByClinicianId(Long id) { return persistence.findByClinicianIdOrderByPublishedAtDescIdDesc(id).stream().map(ReviewPersistenceAssembler::toDomain).toList(); }
    @Transactional public Review save(Review value) {
        var entity = value.getId() == null ? new ReviewPersistenceEntity() : persistence.findById(value.getId()).orElseThrow();
        ReviewPersistenceAssembler.copyToEntity(value, entity);
        var saved = persistence.saveAndFlush(entity);
        events.publishAndClear(value);
        return ReviewPersistenceAssembler.toDomain(saved);
    }
}
