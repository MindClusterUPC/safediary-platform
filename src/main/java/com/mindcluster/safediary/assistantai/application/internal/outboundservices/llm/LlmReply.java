package com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm;

import java.util.List;

/**
 * Raw reflection produced by the language model, before domain normalization.
 */
public record LlmReply(String reply, String emotion, Double riskScore, List<LlmDistortion> distortions) {
}
