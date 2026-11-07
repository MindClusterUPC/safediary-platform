package com.mindcluster.safediary.diary.application.internal.queryservices;

import com.mindcluster.safediary.diary.application.queryservices.DiaryEntryQueryService;
import com.mindcluster.safediary.diary.domain.model.aggregates.DiaryEntry;
import com.mindcluster.safediary.diary.domain.model.queries.GetAllDiaryEntriesByPatientIdQuery;
import com.mindcluster.safediary.diary.domain.model.queries.GetDiaryEntryByIdQuery;
import com.mindcluster.safediary.diary.domain.repositories.DiaryEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class DiaryEntryQueryServiceImpl implements DiaryEntryQueryService {

    private final DiaryEntryRepository repository;

    public DiaryEntryQueryServiceImpl(DiaryEntryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DiaryEntry> handle(GetAllDiaryEntriesByPatientIdQuery query) {
        return repository.findAllByPatientId(query.patientId());
    }

    @Override
    public Optional<DiaryEntry> handle(GetDiaryEntryByIdQuery query) {
        return repository.findById(query.id());
    }
}
