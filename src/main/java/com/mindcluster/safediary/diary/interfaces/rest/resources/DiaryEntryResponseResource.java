package com.mindcluster.safediary.diary.interfaces.rest.resources;

import java.time.LocalDateTime;

public record DiaryEntryResponseResource(
        Long id,
        Long patientId,
        String title,
        String content,
        boolean vaulted,
        LocalDateTime entryDate
) {}
