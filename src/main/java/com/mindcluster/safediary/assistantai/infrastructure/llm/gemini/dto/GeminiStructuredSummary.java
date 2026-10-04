package com.mindcluster.safediary.assistantai.infrastructure.llm.gemini.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Structured weekly summary returned by Gemini.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiStructuredSummary(String narrative, List<String> keyTriggers, List<String> highlights) {
}
