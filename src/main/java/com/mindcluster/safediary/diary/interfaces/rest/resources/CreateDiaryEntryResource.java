package com.mindcluster.safediary.diary.interfaces.rest.resources;

import jakarta.validation.constraints.Size;

public record CreateDiaryEntryResource(
        Long patientId,
        @Size(max = 120, message = "Title cannot exceed 120 characters")
        String title,
        String content,
        Boolean vaulted
) {}
