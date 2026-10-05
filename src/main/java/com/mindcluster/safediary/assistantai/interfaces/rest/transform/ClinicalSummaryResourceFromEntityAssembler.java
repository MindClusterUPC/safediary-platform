package com.mindcluster.safediary.assistantai.interfaces.rest.transform;

import com.mindcluster.safediary.assistantai.domain.model.aggregates.ClinicalSummary;
import com.mindcluster.safediary.assistantai.interfaces.rest.resources.ClinicalSummaryResource;

/**
 * Assembler from clinical summary aggregate to resource.
 */
public final class ClinicalSummaryResourceFromEntityAssembler {

    private ClinicalSummaryResourceFromEntityAssembler() {
    }

    public static ClinicalSummaryResource toResource(ClinicalSummary summary) {
        return new ClinicalSummaryResource(summary.getId(), summary.getAccountId(), summary.getPeriodStart(),
                summary.getPeriodEnd(), summary.getDominantEmotions().stream().map(Enum::name).toList(),
                summary.getKeyTriggers(), summary.getSynthesisNarrative(), summary.getHighlights(),
                summary.getGeneratedAt());
    }
}
