package com.mindcluster.safediary.assistantai.infrastructure.llm.gemini.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Candidate answer returned by Gemini.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiCandidate(GeminiContent content, String finishReason) {
}
