package com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm;

/**
 * Raw cognitive distortion detected by the language model.
 */
public record LlmDistortion(String type, String evidence, Double confidence) {
}
