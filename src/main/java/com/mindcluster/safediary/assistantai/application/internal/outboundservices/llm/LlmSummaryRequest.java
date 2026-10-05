package com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PlutchikEmotionTag;

import java.util.List;

/**
 * Input of a weekly clinical summary request.
 */
public record LlmSummaryRequest(String locale, List<PlutchikEmotionTag> dominantEmotions, List<String> userMessages) {
}
