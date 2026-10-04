package com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm;

import java.util.List;

/**
 * Raw weekly clinical summary produced by the language model.
 */
public record LlmSummary(String narrative, List<String> keyTriggers, List<String> highlights) {
}
