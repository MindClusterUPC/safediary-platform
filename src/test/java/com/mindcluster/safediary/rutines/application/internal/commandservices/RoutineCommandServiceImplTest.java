package com.mindcluster.safediary.rutines.application.internal.commandservices;

import com.mindcluster.safediary.rutines.domain.model.aggregates.DailyRoutine;
import com.mindcluster.safediary.rutines.domain.model.aggregates.SosExercise;
import com.mindcluster.safediary.rutines.domain.model.commands.CreateDailyRoutineCommand;
import com.mindcluster.safediary.rutines.domain.model.commands.StartSosExerciseCommand;
import com.mindcluster.safediary.rutines.domain.model.commands.ToggleDailyRoutineActiveCommand;
import com.mindcluster.safediary.rutines.domain.model.commands.UpdateDailyRoutineCommand;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.ExerciseType;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.FrequencyDays;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.NotificationStatus;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.RoutineTitle;
import com.mindcluster.safediary.rutines.domain.repositories.DailyRoutineRepository;
import com.mindcluster.safediary.rutines.domain.repositories.PromptReflectionRepository;
import com.mindcluster.safediary.rutines.domain.repositories.SosExerciseRepository;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutineCommandServiceImplTest {

    @Mock
    private DailyRoutineRepository dailyRoutineRepository;

    @Mock
    private SosExerciseRepository sosExerciseRepository;

    @Mock
    private PromptReflectionRepository promptReflectionRepository;

    @InjectMocks
    private RoutineCommandServiceImpl routineCommandService;

    @Test
    @DisplayName("handle(CreateDailyRoutineCommand) saves and returns daily routine on valid input")
    void handle_createDailyRoutine_whenValid_savesAndReturnsRoutine() {
        var command = new CreateDailyRoutineCommand(
                1L,
                "Meditacion matutina",
                Set.of(FrequencyDays.MONDAY, FrequencyDays.WEDNESDAY),
                true
        );

        when(dailyRoutineRepository.save(any(DailyRoutine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = routineCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        var routine = ((Result.Success<DailyRoutine, ?>) result).value();
        assertThat(routine.getTitle().value()).isEqualTo("Meditacion matutina");
        assertThat(routine.getNotificationStatus()).isEqualTo(NotificationStatus.ENABLED);
        verify(dailyRoutineRepository).save(any(DailyRoutine.class));
    }

    @Test
    @DisplayName("handle(CreateDailyRoutineCommand) returns validation error when title exceeds 60 chars")
    void handle_createDailyRoutine_whenTitleInvalid_returnsValidationError() {
        var command = new CreateDailyRoutineCommand(
                1L,
                "A".repeat(65),
                Set.of(FrequencyDays.MONDAY),
                true
        );

        var result = routineCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Failure.class);
        var failure = (Result.Failure<?, ApplicationError>) result;
        assertThat(failure.error().code()).isEqualTo("VALIDATION_ERROR");
        verify(dailyRoutineRepository, never()).save(any());
    }

    @Test
    @DisplayName("handle(UpdateDailyRoutineCommand) updates title and status when routine exists")
    void handle_updateDailyRoutine_whenFound_updatesAndReturnsRoutine() {
        var routine = new DailyRoutine(
                10L,
                1L,
                RoutineTitle.of("Paseo vespertino"),
                Set.of(FrequencyDays.FRIDAY),
                NotificationStatus.DISABLED,
                true
        );

        var command = new UpdateDailyRoutineCommand(10L, "Caminata al aire libre", true);

        when(dailyRoutineRepository.findById(10L)).thenReturn(Optional.of(routine));
        when(dailyRoutineRepository.save(any(DailyRoutine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = routineCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        var updated = ((Result.Success<DailyRoutine, ?>) result).value();
        assertThat(updated.getTitle().value()).isEqualTo("Caminata al aire libre");
        assertThat(updated.getNotificationStatus()).isEqualTo(NotificationStatus.ENABLED);
        verify(dailyRoutineRepository).save(routine);
    }

    @Test
    @DisplayName("handle(UpdateDailyRoutineCommand) returns not found when routine missing")
    void handle_updateDailyRoutine_whenNotFound_returnsNotFoundError() {
        var command = new UpdateDailyRoutineCommand(999L, "Nueva rutina", false);
        when(dailyRoutineRepository.findById(999L)).thenReturn(Optional.empty());

        var result = routineCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Failure.class);
        var failure = (Result.Failure<?, ApplicationError>) result;
        assertThat(failure.error().code()).isEqualTo("DAILYROUTINE_NOT_FOUND");
        verify(dailyRoutineRepository, never()).save(any());
    }

    @Test
    @DisplayName("handle(ToggleDailyRoutineActiveCommand) toggles routine active status")
    void handle_toggleDailyRoutineActive_whenFound_togglesStatus() {
        var routine = new DailyRoutine(
                10L,
                1L,
                RoutineTitle.of("Lectura"),
                Set.of(FrequencyDays.SATURDAY),
                NotificationStatus.ENABLED,
                true
        );

        var command = new ToggleDailyRoutineActiveCommand(10L);

        when(dailyRoutineRepository.findById(10L)).thenReturn(Optional.of(routine));
        when(dailyRoutineRepository.save(any(DailyRoutine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = routineCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        var toggled = ((Result.Success<DailyRoutine, ?>) result).value();
        assertThat(toggled.isActive()).isFalse();
        verify(dailyRoutineRepository).save(routine);
    }

    @Test
    @DisplayName("handle(ToggleDailyRoutineActiveCommand) returns not found when routine missing")
    void handle_toggleDailyRoutineActive_whenNotFound_returnsNotFoundError() {
        var command = new ToggleDailyRoutineActiveCommand(404L);
        when(dailyRoutineRepository.findById(404L)).thenReturn(Optional.empty());

        var result = routineCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Failure.class);
        var failure = (Result.Failure<?, ApplicationError>) result;
        assertThat(failure.error().code()).isEqualTo("DAILYROUTINE_NOT_FOUND");
        verify(dailyRoutineRepository, never()).save(any());
    }

    @Test
    @DisplayName("handle(StartSosExerciseCommand) starts new SOS exercise and returns success")
    void handle_startSosExercise_whenValid_savesAndReturnsSosExercise() {
        var command = new StartSosExerciseCommand(1L, ExerciseType.of("RESPIRACION_CONSCIENTE"), 5);

        when(sosExerciseRepository.save(any(SosExercise.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = routineCommandService.handle(command);

        assertThat(result).isInstanceOf(Result.Success.class);
        var exercise = ((Result.Success<SosExercise, ?>) result).value();
        assertThat(exercise.getExerciseType().value()).isEqualTo("RESPIRACION_CONSCIENTE");
        assertThat(exercise.getStepMetrics().totalSteps()).isEqualTo(5);
        verify(sosExerciseRepository).save(any(SosExercise.class));
    }
}
