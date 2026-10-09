package com.mindcluster.safediary.rutines.application.queryservices;

import com.mindcluster.safediary.rutines.domain.model.aggregates.DailyRoutine;
import com.mindcluster.safediary.rutines.domain.model.aggregates.PromptReflection;
import com.mindcluster.safediary.rutines.domain.model.queries.GetAllRoutinesByUserIdQuery;
import com.mindcluster.safediary.rutines.domain.model.queries.GetPendingDailyReflectionQuery;
import com.mindcluster.safediary.rutines.domain.model.queries.GetRoutineByIdQuery;

import java.util.List;
import java.util.Optional;

public interface RoutineQueryService {
    List<DailyRoutine> handle(GetAllRoutinesByUserIdQuery query);
    Optional<PromptReflection> handle(GetPendingDailyReflectionQuery query);
    Optional<DailyRoutine> handle(GetRoutineByIdQuery query);
}