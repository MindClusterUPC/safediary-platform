package com.mindcluster.safediary.rutines.interfaces.rest.resources;

import com.mindcluster.safediary.rutines.domain.model.valueobjects.FrequencyDays;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateDailyRoutineResource(
        @NotNull
        @Schema(description = "Patient ID related to the routine", example = "1")
        Long patientId,

        @NotBlank @Size(max = 60)
        @Schema(description = "Descriptive title of the routine", example = "Take medicine")
        String title,

        @NotEmpty
        @Schema(description = "Days of the week when the routine applies")
        Set<FrequencyDays> frequencyDays,

        @NotNull
        @Schema(description = "Indicates if push notifications are active for this routine", example = "true")
        Boolean isNotificationActive
) {}