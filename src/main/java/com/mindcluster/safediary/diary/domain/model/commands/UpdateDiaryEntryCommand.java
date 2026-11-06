package com.mindcluster.safediary.diary.domain.model.commands;

public record UpdateDiaryEntryCommand(
        Long id,
        Long patientId,
        String title,
        String content
) {}
