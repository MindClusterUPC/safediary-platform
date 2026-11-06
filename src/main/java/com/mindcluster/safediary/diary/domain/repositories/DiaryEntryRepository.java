package com.mindcluster.safediary.diary.domain.repositories;

import com.mindcluster.safediary.diary.domain.model.aggregates.DiaryEntry;

import java.util.List;
import java.util.Optional;

public interface DiaryEntryRepository {
    DiaryEntry save(DiaryEntry entry);
    Optional<DiaryEntry> findById(Long id);
    List<DiaryEntry> findAllByPatientId(Long patientId);
    void delete(DiaryEntry entry);
}
