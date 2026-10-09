package com.mindcluster.safediary.rutines.application.queryservices;

import com.mindcluster.safediary.rutines.domain.model.aggregates.DailyRoutine;
import com.mindcluster.safediary.rutines.domain.model.aggregates.PromptReflection;
import com.mindcluster.safediary.rutines.domain.model.aggregates.SosExercise;
import com.mindcluster.safediary.rutines.domain.model.queries.GetAllRoutinesByUserIdQuery;
import com.mindcluster.safediary.rutines.domain.model.queries.GetDailyRoutineByIdQuery;
import com.mindcluster.safediary.rutines.domain.model.queries.GetPendingDailyReflectionQuery;
import com.mindcluster.safediary.rutines.domain.model.queries.GetPromptReflectionByIdQuery;
import com.mindcluster.safediary.rutines.domain.model.queries.GetRoutineByIdQuery;
import com.mindcluster.safediary.rutines.domain.model.queries.GetSosExerciseByIdQuery;
import com.mindcluster.safediary.rutines.domain.model.queries.GetSosExerciseHistoryByPatientIdQuery;

import java.util.List;
import java.util.Optional;

public interface RoutineQueryService {
    List<DailyRoutine> handle(GetAllRoutinesByUserIdQuery query);
    Optional<DailyRoutine> handle(GetRoutineByIdQuery query);
    Optional<DailyRoutine> handle(GetDailyRoutineByIdQuery query);
    Optional<PromptReflection> handle(GetPendingDailyReflectionQuery query);
    Optional<PromptReflection> handle(GetPromptReflectionByIdQuery query);
    Optional<SosExercise> handle(GetSosExerciseByIdQuery query);
    List<SosExercise> handle(GetSosExerciseHistoryByPatientIdQuery query);
}