package com.mindcluster.safediary.rutines.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StartSosExerciseResource(
        @NotNull
        @Schema(description = "ID of the patient initiating the SOS exercise", example = "1")
        Long patientId,

        @NotBlank @Size(max = 100)
        @Schema(description = "Type or name of the SOS exercise being performed", example = "5-4-3-2-1 Grounding")
        String exerciseType,

        @NotNull @Min(1)
        @Schema(description = "Total number of steps required to complete this exercise", example = "5")
        Integer totalSteps
) {}