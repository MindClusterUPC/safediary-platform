package com.mindcluster.safediary.rutines.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitDailyReflectionResource(
        @NotBlank 
        @Size(max = 2000)
        @Schema(description = "Free text with the patient's response to the reflection question", example = "Today I felt grateful for the support of my family and friends. It reminded me of the importance of staying connected and appreciating the little things in life.")
        String answer
) {}