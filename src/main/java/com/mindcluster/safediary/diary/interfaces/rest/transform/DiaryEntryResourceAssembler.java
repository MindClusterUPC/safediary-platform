package com.mindcluster.safediary.diary.interfaces.rest.transform;

import com.mindcluster.safediary.diary.domain.model.aggregates.DiaryEntry;
import com.mindcluster.safediary.diary.interfaces.rest.resources.DiaryEntryResponseResource;

public final class DiaryEntryResourceAssembler {

    private DiaryEntryResourceAssembler() {}

    public static DiaryEntryResponseResource toResource(DiaryEntry domain) {
        if (domain == null) return null;
        return new DiaryEntryResponseResource(
                domain.getId(),
                domain.getPatientId(),
                domain.getTitle(),
                domain.getContent(),
                domain.isVaulted(),
                domain.getEntryDate()
        );
    }
}
