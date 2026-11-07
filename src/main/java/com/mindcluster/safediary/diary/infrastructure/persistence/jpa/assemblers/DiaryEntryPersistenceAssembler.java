package com.mindcluster.safediary.diary.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.diary.domain.model.aggregates.DiaryEntry;
import com.mindcluster.safediary.diary.infrastructure.persistence.jpa.entities.DiaryEntryPersistenceEntity;

public final class DiaryEntryPersistenceAssembler {

    private DiaryEntryPersistenceAssembler() {}

    public static DiaryEntry toDomain(DiaryEntryPersistenceEntity entity) {
        if (entity == null) return null;
        return new DiaryEntry(
                entity.getId(),
                entity.getPatientId(),
                entity.getTitle(),
                entity.getContent(),
                entity.isVaulted(),
                entity.getEntryDate()
        );
    }

    public static void copyToEntity(DiaryEntry domain, DiaryEntryPersistenceEntity entity) {
        if (domain == null || entity == null) return;
        entity.setPatientId(domain.getPatientId());
        entity.setTitle(domain.getTitle());
        entity.setContent(domain.getContent());
        entity.setVaulted(domain.isVaulted());
        entity.setEntryDate(domain.getEntryDate());
    }
}
