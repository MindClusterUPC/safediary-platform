package com.mindcluster.safediary.rutines.domain.model.valueobjects;

public record StepMetrics(Integer currentStep, Integer totalSteps) {
    public StepMetrics {
        if (currentStep == null || currentStep < 0) {
            throw new IllegalArgumentException("Current step cannot be null or negative");
        }
        if (totalSteps == null || totalSteps < 1) {
            throw new IllegalArgumentException("Total steps must be at least 1");
        }
        if (currentStep > totalSteps) {
            throw new IllegalArgumentException("Current step cannot exceed total steps");
        }
    }

    public static StepMetrics of(Integer currentStep, Integer totalSteps) {
        return new StepMetrics(currentStep, totalSteps);
    }

    public static StepMetrics initial(Integer totalSteps) {
        return new StepMetrics(1, totalSteps);
    }

    public StepMetrics advance() {
        if (currentStep < totalSteps) {
            return new StepMetrics(currentStep + 1, totalSteps);
        }
        return this;
    }

    public boolean isCompleted() {
        return currentStep.equals(totalSteps);
    }
}
