package com.mindcluster.safediary.diary.domain.model.commands;

public record CreateDiaryEntryCommand(
        Long patientId,
        String title,
        String content,
        boolean vaulted
) {}
