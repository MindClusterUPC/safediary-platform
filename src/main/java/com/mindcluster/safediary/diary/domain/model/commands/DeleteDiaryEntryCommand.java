package com.mindcluster.safediary.diary.domain.model.commands;

public record DeleteDiaryEntryCommand(
        Long id,
        Long patientId
) {}
