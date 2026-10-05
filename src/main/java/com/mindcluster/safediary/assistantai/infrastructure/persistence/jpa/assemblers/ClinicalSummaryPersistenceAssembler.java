package com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.assemblers;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ClinicalSummary;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PlutchikEmotionTag;
import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.entities.ClinicalSummaryPersistenceEntity;

import java.util.Arrays;
import java.util.List;

/**
 * Static assembler between clinical summary domain and persistence representations.
 */
public final class ClinicalSummaryPersistenceAssembler {

    private ClinicalSummaryPersistenceAssembler() {
    }

    public static ClinicalSummary toDomainFromPersistence(ClinicalSummaryPersistenceEntity entity) {
        var emotions = split(entity.getDominantEmotions(), ",").stream().map(PlutchikEmotionTag::valueOf).toList();
        return new ClinicalSummary(entity.getId(), entity.getAccountId(), entity.getPeriodStart(),
                entity.getPeriodEnd(), emotions, split(entity.getKeyTriggers(), "\n"),
                entity.getSynthesisNarrative(), split(entity.getHighlights(), "\n"), entity.getGeneratedAt());
    }

    public static ClinicalSummaryPersistenceEntity toPersistenceFromDomain(ClinicalSummary summary) {
        var entity = new ClinicalSummaryPersistenceEntity();
        if (summary.getId() != null) entity.setId(summary.getId());
        entity.setAccountId(summary.getAccountId());
        entity.setPeriodStart(summary.getPeriodStart());
        entity.setPeriodEnd(summary.getPeriodEnd());
        entity.setDominantEmotions(String.join(",", summary.getDominantEmotions().stream().map(Enum::name).toList()));
        entity.setKeyTriggers(String.join("\n", summary.getKeyTriggers()));
        entity.setSynthesisNarrative(summary.getSynthesisNarrative());
        entity.setHighlights(String.join("\n", summary.getHighlights()));
        entity.setGeneratedAt(summary.getGeneratedAt());
        return entity;
    }

    private static List<String> split(String value, String separator) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(separator)).filter(v -> !v.isBlank()).toList();
    }
}
