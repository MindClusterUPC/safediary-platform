package com.mindcluster.safediary.rutines.interfaces.rest.resources;

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
        Boolean isNotificationActive
) {}