package com.mindcluster.safediary.assistantai.infrastructure.llm.gemini.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Gemini generation config with JSON structured output.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiGenerationConfig(Double temperature, String responseMimeType, Map<String, Object> responseSchema) {
}
