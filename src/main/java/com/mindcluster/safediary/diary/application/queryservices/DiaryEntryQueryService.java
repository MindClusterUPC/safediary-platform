package com.mindcluster.safediary.diary.application.queryservices;

import com.mindcluster.safediary.diary.domain.model.aggregates.DiaryEntry;
import com.mindcluster.safediary.diary.domain.model.queries.GetAllDiaryEntriesByPatientIdQuery;
import com.mindcluster.safediary.diary.domain.model.queries.GetDiaryEntryByIdQuery;

import java.util.List;
import java.util.Optional;

public interface DiaryEntryQueryService {
    List<DiaryEntry> handle(GetAllDiaryEntriesByPatientIdQuery query);
    Optional<DiaryEntry> handle(GetDiaryEntryByIdQuery query);
}
