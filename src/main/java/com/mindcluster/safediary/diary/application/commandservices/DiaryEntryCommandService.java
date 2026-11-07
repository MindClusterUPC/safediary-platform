package com.mindcluster.safediary.diary.application.commandservices;

import com.mindcluster.safediary.diary.domain.model.aggregates.DiaryEntry;
import com.mindcluster.safediary.diary.domain.model.commands.CreateDiaryEntryCommand;
import com.mindcluster.safediary.diary.domain.model.commands.DeleteDiaryEntryCommand;
import com.mindcluster.safediary.diary.domain.model.commands.UpdateDiaryEntryCommand;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;

public interface DiaryEntryCommandService {
    Result<DiaryEntry, ApplicationError> handle(CreateDiaryEntryCommand command);
    Result<DiaryEntry, ApplicationError> handle(UpdateDiaryEntryCommand command);
    Result<Void, ApplicationError> handle(DeleteDiaryEntryCommand command);
}
