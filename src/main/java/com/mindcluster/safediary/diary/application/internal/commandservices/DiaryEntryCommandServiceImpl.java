package com.mindcluster.safediary.diary.application.internal.commandservices;

import com.mindcluster.safediary.diary.application.commandservices.DiaryEntryCommandService;
import com.mindcluster.safediary.diary.domain.model.aggregates.DiaryEntry;
import com.mindcluster.safediary.diary.domain.model.commands.CreateDiaryEntryCommand;
import com.mindcluster.safediary.diary.domain.model.commands.DeleteDiaryEntryCommand;
import com.mindcluster.safediary.diary.domain.model.commands.UpdateDiaryEntryCommand;
import com.mindcluster.safediary.diary.domain.repositories.DiaryEntryRepository;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiaryEntryCommandServiceImpl implements DiaryEntryCommandService {

    private final DiaryEntryRepository repository;

    public DiaryEntryCommandServiceImpl(DiaryEntryRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Result<DiaryEntry, ApplicationError> handle(CreateDiaryEntryCommand command) {
        if (command.patientId() == null) {
            return Result.failure(ApplicationError.validationError("patientId", "patientId is required"));
        }
        var entry = new DiaryEntry(command.patientId(), command.title(), command.content(), command.vaulted());
        var saved = repository.save(entry);
        return Result.success(saved);
    }

    @Override
    @Transactional
    public Result<DiaryEntry, ApplicationError> handle(UpdateDiaryEntryCommand command) {
        var optEntry = repository.findById(command.id());
        if (optEntry.isEmpty()) {
            return Result.failure(ApplicationError.notFound("DiaryEntry", String.valueOf(command.id())));
        }
        var entry = optEntry.get();
        if (command.patientId() != null && !entry.getPatientId().equals(command.patientId())) {
            return Result.failure(ApplicationError.forbidden("Cannot modify a diary entry of another patient"));
        }
        entry.update(command.title(), command.content());
        var saved = repository.save(entry);
        return Result.success(saved);
    }

    @Override
    @Transactional
    public Result<Void, ApplicationError> handle(DeleteDiaryEntryCommand command) {
        var optEntry = repository.findById(command.id());
        if (optEntry.isEmpty()) {
            return Result.failure(ApplicationError.notFound("DiaryEntry", String.valueOf(command.id())));
        }
        var entry = optEntry.get();
        if (command.patientId() != null && !entry.getPatientId().equals(command.patientId())) {
            return Result.failure(ApplicationError.forbidden("Cannot delete a diary entry of another patient"));
        }
        repository.delete(entry);
        return Result.success(null);
    }
}
