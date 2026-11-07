package com.mindcluster.safediary.rutines.application.internal.queryservices;

import com.mindcluster.safediary.rutines.domain.model.aggregates.DailyRoutine;
import com.mindcluster.safediary.rutines.domain.model.aggregates.PromptReflection;
import com.mindcluster.safediary.rutines.domain.model.aggregates.SosExercise;
import com.mindcluster.safediary.rutines.domain.model.queries.*;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.ExerciseType;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.FrequencyDays;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.NotificationStatus;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.PromptText;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.RoutineTitle;
import com.mindcluster.safediary.rutines.domain.repositories.DailyRoutineRepository;
import com.mindcluster.safediary.rutines.domain.repositories.PromptReflectionRepository;
import com.mindcluster.safediary.rutines.domain.repositories.SosExerciseRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutineQueryServiceImplTest {

    @Mock
    private DailyRoutineRepository dailyRoutineRepository;

    @Mock
    private PromptReflectionRepository promptReflectionRepository;

    @Mock
    private SosExerciseRepository sosExerciseRepository;

    @InjectMocks
    private RoutineQueryServiceImpl routineQueryService;

    @Test
    @DisplayName("handle(GetAllRoutinesByUserIdQuery) returns list of routines for given patient")
    void handle_getAllRoutinesByUserId_returnsRoutinesList() {
        var routine = new DailyRoutine(
                1L,
                100L,
                RoutineTitle.of("Diario matutino"),
                Set.of(FrequencyDays.MONDAY),
                NotificationStatus.ENABLED,
                true
        );
        when(dailyRoutineRepository.findAllByPatientId(100L)).thenReturn(List.of(routine));

        var result = routineQueryService.handle(new GetAllRoutinesByUserIdQuery(100L));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle().value()).isEqualTo("Diario matutino");
        verify(dailyRoutineRepository).findAllByPatientId(100L);
    }

    @Test
    @DisplayName("handle(GetDailyRoutineByIdQuery) returns routine when found")
    void handle_getDailyRoutineById_whenFound_returnsRoutine() {
        var routine = new DailyRoutine(
                5L,
                100L,
                RoutineTitle.of("Estiramientos"),
                Set.of(FrequencyDays.TUESDAY),
                NotificationStatus.DISABLED,
                true
        );
        when(dailyRoutineRepository.findById(5L)).thenReturn(Optional.of(routine));

        var result = routineQueryService.handle(new GetDailyRoutineByIdQuery(5L));

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(5L);
        verify(dailyRoutineRepository).findById(5L);
    }

    @Test
    @DisplayName("handle(GetDailyRoutineByIdQuery) returns empty when routine not found")
    void handle_getDailyRoutineById_whenNotFound_returnsEmpty() {
        when(dailyRoutineRepository.findById(999L)).thenReturn(Optional.empty());

        var result = routineQueryService.handle(new GetDailyRoutineByIdQuery(999L));

        assertThat(result).isEmpty();
        verify(dailyRoutineRepository).findById(999L);
    }

    @Test
    @DisplayName("handle(GetPendingDailyReflectionQuery) returns pending reflection when exists")
    void handle_getPendingDailyReflection_whenFound_returnsPromptReflection() {
        var reflection = new PromptReflection(100L, PromptText.of("¿Como te sientes hoy?"));
        when(promptReflectionRepository.findByPatientIdAndStatus(100L, PromptReflection.ReflectionStatus.PENDING))
                .thenReturn(Optional.of(reflection));

        var result = routineQueryService.handle(new GetPendingDailyReflectionQuery(100L));

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(PromptReflection.ReflectionStatus.PENDING);
        verify(promptReflectionRepository).findByPatientIdAndStatus(100L, PromptReflection.ReflectionStatus.PENDING);
    }

    @Test
    @DisplayName("handle(GetSosExerciseByIdQuery) returns exercise when found")
    void handle_getSosExerciseById_whenFound_returnsSosExercise() {
        var exercise = new SosExercise(100L, ExerciseType.of("GROUNDING"), 5);
        when(sosExerciseRepository.findById(10L)).thenReturn(Optional.of(exercise));

        var result = routineQueryService.handle(new GetSosExerciseByIdQuery(10L));

        assertThat(result).isPresent();
        assertThat(result.get().getType().value()).isEqualTo("GROUNDING");
        verify(sosExerciseRepository).findById(10L);
    }

    @Test
    @DisplayName("handle(GetSosExerciseHistoryByPatientIdQuery) returns history list for patient")
    void handle_getSosExerciseHistoryByPatientId_returnsHistoryList() {
        var exercise = new SosExercise(100L, ExerciseType.of("GROUNDING"), 5);
        when(sosExerciseRepository.findAllByPatientId(100L)).thenReturn(List.of(exercise));

        var result = routineQueryService.handle(new GetSosExerciseHistoryByPatientIdQuery(100L));

        assertThat(result).hasSize(1);
        verify(sosExerciseRepository).findAllByPatientId(100L);
    }
}
