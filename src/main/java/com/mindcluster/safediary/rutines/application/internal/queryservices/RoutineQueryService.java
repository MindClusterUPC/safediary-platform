package com.mindcluster.safediary.rutines.application.internal.queryservices;

import com.mindcluster.safediary.rutines.application.queryservices.RoutineQueryService;
import com.mindcluster.safediary.rutines.domain.model.aggregates.DailyRoutine;
import com.mindcluster.safediary.rutines.domain.model.aggregates.PromptReflection;
import com.mindcluster.safediary.rutines.domain.model.queries.GetAllRoutinesByUserIdQuery;
import com.mindcluster.safediary.rutines.domain.model.queries.GetPendingDailyReflectionQuery;
import com.mindcluster.safediary.rutines.domain.model.queries.GetRoutineByIdQuery;
import com.mindcluster.safediary.rutines.domain.repositories.DailyRoutineRepository;
import com.mindcluster.safediary.rutines.domain.repositories.PromptReflectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RoutineQueryServiceImpl implements RoutineQueryService {

    private final DailyRoutineRepository dailyRoutineRepository;
    private final PromptReflectionRepository promptReflectionRepository;

    public RoutineQueryServiceImpl(DailyRoutineRepository dailyRoutineRepository,
                                   PromptReflectionRepository promptReflectionRepository) {
        this.dailyRoutineRepository = dailyRoutineRepository;
        this.promptReflectionRepository = promptReflectionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyRoutine> handle(GetAllRoutinesByUserIdQuery query) {
        return dailyRoutineRepository.findAllByPatientId(query.userId());
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
    public Optional<DailyRoutine> handle(GetRoutineByIdQuery query) {
        return dailyRoutineRepository.findById(query.routineId());
    }
}