package com.mindcluster.safediary.rutines.application.internal.queryservices;

import com.mindcluster.safediary.rutines.application.queryservices.RoutineQueryService;
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
import com.mindcluster.safediary.rutines.domain.repositories.DailyRoutineRepository;
import com.mindcluster.safediary.rutines.domain.repositories.PromptReflectionRepository;
import com.mindcluster.safediary.rutines.domain.repositories.SosExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RoutineQueryServiceImpl implements RoutineQueryService {

    private final DailyRoutineRepository dailyRoutineRepository;
    private final PromptReflectionRepository promptReflectionRepository;
    private final SosExerciseRepository sosExerciseRepository;

    public RoutineQueryServiceImpl(DailyRoutineRepository dailyRoutineRepository,
                                   PromptReflectionRepository promptReflectionRepository,
                                   SosExerciseRepository sosExerciseRepository) {
        this.dailyRoutineRepository = dailyRoutineRepository;
        this.promptReflectionRepository = promptReflectionRepository;
        this.sosExerciseRepository = sosExerciseRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyRoutine> handle(GetAllRoutinesByUserIdQuery query) {
        return dailyRoutineRepository.findAllByPatientId(query.userId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DailyRoutine> handle(GetRoutineByIdQuery query) {
        return dailyRoutineRepository.findById(query.routineId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DailyRoutine> handle(GetDailyRoutineByIdQuery query) {
        return dailyRoutineRepository.findById(query.routineId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PromptReflection> handle(GetPendingDailyReflectionQuery query) {
        return promptReflectionRepository.findByPatientIdAndStatus(
                query.userId(),
                PromptReflection.ReflectionStatus.PENDING
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PromptReflection> handle(GetPromptReflectionByIdQuery query) {
        return promptReflectionRepository.findById(query.reflectionId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SosExercise> handle(GetSosExerciseByIdQuery query) {
        return sosExerciseRepository.findById(query.sosExerciseId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SosExercise> handle(GetSosExerciseHistoryByPatientIdQuery query) {
        return sosExerciseRepository.findAllByPatientId(query.patientId());
    }
}