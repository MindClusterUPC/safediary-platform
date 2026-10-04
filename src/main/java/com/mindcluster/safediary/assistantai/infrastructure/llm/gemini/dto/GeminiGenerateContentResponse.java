package com.mindcluster.safediary.assistantai.infrastructure.llm.gemini.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Response body of the Gemini generateContent endpoint.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiGenerateContentResponse(List<GeminiCandidate> candidates) {
}
