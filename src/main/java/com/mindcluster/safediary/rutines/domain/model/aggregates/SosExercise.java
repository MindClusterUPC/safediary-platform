package com.mindcluster.safediary.rutines.domain.model.aggregates;

import com.mindcluster.safediary.rutines.domain.model.events.SosExerciseCompletedEvent;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.ExerciseType;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.StepMetrics;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class SosExercise extends AbstractDomainAggregateRoot<SosExercise> {

    private Long id;
    private Long patientId;
    private ExerciseType exerciseType;
    private StepMetrics stepMetrics;
    private ExerciseStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public enum ExerciseStatus {
        STARTED,
        COMPLETED,
        ABANDONED
    }

    public SosExercise(Long patientId, ExerciseType exerciseType, Integer totalSteps) {
        this.patientId = Objects.requireNonNull(patientId, "patientId is required");
        this.exerciseType = Objects.requireNonNull(exerciseType, "exerciseType is required");
        this.stepMetrics = StepMetrics.initial(totalSteps);
        this.status = ExerciseStatus.STARTED;
        this.startedAt = LocalDateTime.now();
    }

    public SosExercise(Long patientId, ExerciseType exerciseType, StepMetrics stepMetrics) {
        this.patientId = Objects.requireNonNull(patientId, "patientId is required");
        this.exerciseType = Objects.requireNonNull(exerciseType, "exerciseType is required");
        this.stepMetrics = Objects.requireNonNull(stepMetrics, "stepMetrics is required");
        this.status = ExerciseStatus.STARTED;
        this.startedAt = LocalDateTime.now();
    }

    public SosExercise(Long id, Long patientId, ExerciseType exerciseType, StepMetrics stepMetrics, LocalDateTime completedAt) {
        this(patientId, exerciseType, stepMetrics);
        this.id = id;
        this.completedAt = completedAt;
        if (completedAt != null) {
            this.status = ExerciseStatus.COMPLETED;
        }
    }

    public SosExercise(Long id, Long patientId, ExerciseType exerciseType, StepMetrics stepMetrics, ExerciseStatus status, LocalDateTime startedAt, LocalDateTime completedAt) {
        this.id = id;
        this.patientId = Objects.requireNonNull(patientId, "patientId is required");
        this.exerciseType = Objects.requireNonNull(exerciseType, "exerciseType is required");
        this.stepMetrics = Objects.requireNonNull(stepMetrics, "stepMetrics is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.startedAt = startedAt != null ? startedAt : LocalDateTime.now();
        this.completedAt = completedAt;
    }

    public void advanceStep() {
        if (this.status != ExerciseStatus.STARTED) {
            throw new IllegalStateException("Exercise is not in progress");
        }

        this.stepMetrics = this.stepMetrics.advance();

        if (this.stepMetrics.isCompleted()) {
            complete();
        }
    }

    public void complete() {
        if (this.status != ExerciseStatus.STARTED) {
            throw new IllegalStateException("Only started exercises can be completed");
        }
        this.status = ExerciseStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();

        this.registerEvent(new SosExerciseCompletedEvent(
                this.id,
                this.patientId,
                this.exerciseType.value(),
                this.stepMetrics.totalSteps(),
                this.completedAt
        ));
    }

    public void abandon() {
        if (this.status != ExerciseStatus.STARTED) {
            throw new IllegalStateException("Only started exercises can be abandoned");
        }
        this.status = ExerciseStatus.ABANDONED;
        this.completedAt = LocalDateTime.now();
    }

    public ExerciseType getType() {
        return exerciseType;
    }

    public Integer getCurrentStep() {
        return stepMetrics != null ? stepMetrics.currentStep() : null;
    }

    public Integer getTotalSteps() {
        return stepMetrics != null ? stepMetrics.totalSteps() : null;
    }
}