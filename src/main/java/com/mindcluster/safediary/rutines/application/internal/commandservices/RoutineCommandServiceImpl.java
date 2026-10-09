package com.mindcluster.safediary.rutines.application.internal.commandservices;

import com.mindcluster.safediary.rutines.application.commandservices.RoutineCommandService;
import com.mindcluster.safediary.rutines.domain.model.aggregates.DailyRoutine;
import com.mindcluster.safediary.rutines.domain.model.aggregates.PromptReflection;
import com.mindcluster.safediary.rutines.domain.model.aggregates.SosExercise;
import com.mindcluster.safediary.rutines.domain.model.commands.*;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.NotificationStatus;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.PromptText;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.ReflectionAnswer;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.RoutineTitle;
import com.mindcluster.safediary.rutines.domain.repositories.DailyRoutineRepository;
import com.mindcluster.safediary.rutines.domain.repositories.PromptReflectionRepository;
import com.mindcluster.safediary.rutines.domain.repositories.SosExerciseRepository;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoutineCommandServiceImpl implements RoutineCommandService {

    private final DailyRoutineRepository dailyRoutineRepository;
    private final SosExerciseRepository sosExerciseRepository;
    private final PromptReflectionRepository promptReflectionRepository;

    public RoutineCommandServiceImpl(DailyRoutineRepository dailyRoutineRepository,
                                     SosExerciseRepository sosExerciseRepository,
                                     PromptReflectionRepository promptReflectionRepository) {
        this.dailyRoutineRepository = dailyRoutineRepository;
        this.sosExerciseRepository = sosExerciseRepository;
        this.promptReflectionRepository = promptReflectionRepository;
    }

    @Override
    @Transactional
    public Result<DailyRoutine, ApplicationError> handle(CreateDailyRoutineCommand command) {
        try {
            var titleVo = RoutineTitle.of(command.title());
            var notificationStatusVo = command.isNotificationActive() ?
                    NotificationStatus.ENABLED : NotificationStatus.DISABLED;

            var routine = new DailyRoutine(command.patientId(), titleVo, command.frequencyDays(), notificationStatusVo);
            var saved = dailyRoutineRepository.save(routine);
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("daily-routine-creation", ex.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("daily-routine-creation", ex.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<DailyRoutine, ApplicationError> handle(UpdateDailyRoutineCommand command) {
        try {
            var routineOpt = dailyRoutineRepository.findById(command.routineId());
            if (routineOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("DailyRoutine", String.valueOf(command.routineId())));
            }
            var routine = routineOpt.get();

            var newTitle = RoutineTitle.of(command.title());
            var newStatus = command.isNotificationActive() ?
                    NotificationStatus.ENABLED : NotificationStatus.DISABLED;

            routine.updateTitle(newTitle);
            routine.changeNotificationStatus(newStatus);

            var saved = dailyRoutineRepository.save(routine);
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("daily-routine-update", ex.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("daily-routine-update", ex.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<DailyRoutine, ApplicationError> handle(ToggleRoutineNotificationCommand command) {
        try {
            var routineOpt = dailyRoutineRepository.findById(command.routineId());
            if (routineOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("DailyRoutine", String.valueOf(command.routineId())));
            }
            var routine = routineOpt.get();

            var newStatus = command.isEnabled() ?
                    NotificationStatus.ENABLED : NotificationStatus.DISABLED;
            routine.changeNotificationStatus(newStatus);

            var saved = dailyRoutineRepository.save(routine);
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("daily-routine-toggle", ex.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("daily-routine-toggle", ex.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<DailyRoutine, ApplicationError> handle(ToggleDailyRoutineActiveCommand command) {
        try {
            var routineOpt = dailyRoutineRepository.findById(command.routineId());
            if (routineOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("DailyRoutine", String.valueOf(command.routineId())));
            }
            var routine = routineOpt.get();

            if (routine.isActive()) {
                routine.deactivateRoutine();
            } else {
                routine.activateRoutine();
            }

            var saved = dailyRoutineRepository.save(routine);
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("daily-routine-toggle-active", ex.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("daily-routine-toggle-active", ex.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<SosExercise, ApplicationError> handle(StartSosExerciseCommand command) {
        try {
            var exercise = new SosExercise(command.patientId(), command.type(), command.totalSteps());
            var saved = sosExerciseRepository.save(exercise);
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("sos-exercise-start", ex.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("sos-exercise-start", ex.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<SosExercise, ApplicationError> handle(AdvanceSosStepCommand command) {
        try {
            var exerciseOpt = sosExerciseRepository.findById(command.sosExerciseId());
            if (exerciseOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("SosExercise", String.valueOf(command.sosExerciseId())));
            }
            var exercise = exerciseOpt.get();

            exercise.advanceStep();

            var saved = sosExerciseRepository.save(exercise);
            return Result.success(saved);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.failure(ApplicationError.validationError("sos-exercise-advance", ex.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("sos-exercise-advance", ex.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<SosExercise, ApplicationError> handle(CompleteSosExerciseCommand command) {
        try {
            var exerciseOpt = sosExerciseRepository.findById(command.sosExerciseId());
            if (exerciseOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("SosExercise", String.valueOf(command.sosExerciseId())));
            }
            var exercise = exerciseOpt.get();

            exercise.complete();

            var saved = sosExerciseRepository.save(exercise);
            return Result.success(saved);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.failure(ApplicationError.validationError("sos-exercise-complete", ex.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("sos-exercise-complete", ex.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<PromptReflection, ApplicationError> handle(CreatePromptReflectionCommand command) {
        try {
            var promptTextVo = PromptText.of(command.promptText());
            var reflection = new PromptReflection(command.patientId(), promptTextVo);
            var saved = promptReflectionRepository.save(reflection);
            return Result.success(saved);
        } catch (IllegalArgumentException ex) {
            return Result.failure(ApplicationError.validationError("prompt-reflection-creation", ex.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("prompt-reflection-creation", ex.getMessage()));
        }
    }

    @Override
    @Transactional
    public Result<PromptReflection, ApplicationError> handle(SubmitDailyReflectionCommand command) {
        try {
            var reflectionOpt = promptReflectionRepository.findById(command.reflectionId());
            if (reflectionOpt.isEmpty()) {
                return Result.failure(ApplicationError.notFound("PromptReflection", String.valueOf(command.reflectionId())));
            }
            var reflection = reflectionOpt.get();

            reflection.submit(ReflectionAnswer.of(command.answer()));

            var saved = promptReflectionRepository.save(reflection);
            return Result.success(saved);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.failure(ApplicationError.validationError("prompt-reflection-submit", ex.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.unexpected("prompt-reflection-submit", ex.getMessage()));
        }
    }
}