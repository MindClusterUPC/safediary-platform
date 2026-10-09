package com.mindcluster.safediary.rutines.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ToggleRoutineNotificationResource(
        @NotNull
        @Schema(description = "Shows whether the notification is enabled", example = "true")
        Boolean isEnabled
) {}