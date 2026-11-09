package com.mindcluster.safediary.rutines.interfaces.rest.resources;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import com.mindcluster.safediary.rutines.domain.model.valueobjects.FrequencyDays;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDailyRoutineResource(
        @NotBlank @Size(max = 60)
        @Schema(description = "Updated descriptive title of the routine", example = "Do exercise in the afternoon")
        String title,

        @NotNull
        @Schema(description = "Indicates if push notifications are active for this routine", example = "false")
        Boolean isNotificationActive,
        @Size(min = 1) Set<FrequencyDays> frequencyDays,
        @Size(max = 1000) String description,
        @Pattern(regexp = "([01][0-9]|2[0-3]):[0-5][0-9]") String time,
        @Min(1) @Max(60) Integer minutes,
        @Pattern(regexp = "BREATHING|GROUNDING") String kind
) {}
