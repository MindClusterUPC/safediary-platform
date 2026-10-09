package com.mindcluster.safediary.rutines.application.commandservices;

import com.mindcluster.safediary.rutines.domain.model.aggregates.DailyRoutine;
import com.mindcluster.safediary.rutines.domain.model.aggregates.PromptReflection;
import com.mindcluster.safediary.rutines.domain.model.aggregates.SosExercise;
import com.mindcluster.safediary.rutines.domain.model.commands.*;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;

public interface RoutineCommandService {
    Result<DailyRoutine, ApplicationError> handle(CreateDailyRoutineCommand command);
    Result<DailyRoutine, ApplicationError> handle(UpdateDailyRoutineCommand command);
    Result<DailyRoutine, ApplicationError> handle(ToggleRoutineNotificationCommand command);
    Result<DailyRoutine, ApplicationError> handle(ToggleDailyRoutineActiveCommand command);
    Result<SosExercise, ApplicationError> handle(StartSosExerciseCommand command);
    Result<SosExercise, ApplicationError> handle(AdvanceSosStepCommand command);
    Result<SosExercise, ApplicationError> handle(CompleteSosExerciseCommand command);
    Result<PromptReflection, ApplicationError> handle(CreatePromptReflectionCommand command);
    Result<PromptReflection, ApplicationError> handle(SubmitDailyReflectionCommand command);
}