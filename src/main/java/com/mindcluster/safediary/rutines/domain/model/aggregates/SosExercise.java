package com.mindcluster.safediary.rutines.domain.model.aggregates;

import com.mindcluster.safediary.rutines.domain.model.valueobjects.ExerciseType;
import com.mindcluster.safediary.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class SosExercise extends AbstractDomainAggregateRoot<SosExercise> {

    private Long id;
    private Long patientId;
    private ExerciseType type;
    private Integer currentStep;
    private Integer totalSteps;
    private ExerciseStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public enum ExerciseStatus {
        STARTED,
        COMPLETED,
        ABANDONED
    }

    public SosExercise(Long patientId, ExerciseType type, Integer totalSteps) {
        this.patientId = Objects.requireNonNull(patientId, "patientId is required");
        this.type = Objects.requireNonNull(type, "type is required");
        this.totalSteps = Objects.requireNonNull(totalSteps, "totalSteps is required");
        
        if (this.totalSteps < 1) {
            throw new IllegalArgumentException("totalSteps must be at least 1");
        }

        this.currentStep = 1;
        this.status = ExerciseStatus.STARTED;
        this.startedAt = LocalDateTime.now();
    }

    public SosExercise(Long id, Long patientId, ExerciseType type, Integer currentStep, Integer totalSteps, ExerciseStatus status, LocalDateTime startedAt, LocalDateTime completedAt) {
        this(patientId, type, totalSteps);
        this.id = id;
        this.currentStep = Objects.requireNonNull(currentStep, "currentStep is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.startedAt = Objects.requireNonNull(startedAt, "startedAt is required");
        this.completedAt = completedAt;
    }

    public void advanceStep() {
        if (this.status != ExerciseStatus.STARTED) {
            throw new IllegalStateException("Exercise is not in progress");
        }
        
        if (this.currentStep < this.totalSteps) {
            this.currentStep++;
        }
        
        if (this.currentStep.equals(this.totalSteps)) {
            complete();
        }
    }

    public void complete() {
        if (this.status != ExerciseStatus.STARTED) {
            throw new IllegalStateException("Only started exercises can be completed");
        }
        this.status = ExerciseStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void abandon() {
        if (this.status != ExerciseStatus.STARTED) {
            throw new IllegalStateException("Only started exercises can be abandoned");
        }
        this.status = ExerciseStatus.ABANDONED;
        this.completedAt = LocalDateTime.now();
    }
}