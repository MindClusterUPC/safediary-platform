package com.mindcluster.safediary.assistantai.infrastructure.llm.gemini;

import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.*;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.MessageSender;
import com.mindcluster.safediary.assistantai.infrastructure.llm.gemini.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Anti-corruption layer over the Google Gemini generateContent API.
 * <p>
 * Translates Gemini's contract into the AssistantAI model. The API key travels only in a header.
 * </p>
 */
@Component
@ConditionalOnProperty(name = "assistantai.llm.provider", havingValue = "gemini")
public class GeminiLlmAdapter implements AssistantLanguageModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeminiLlmAdapter.class);

    private final RestClient restClient;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final String model;
    private final String fallbackModel;

    public GeminiLlmAdapter(@Value("${assistantai.llm.gemini.api-key}") String apiKey,
                            @Value("${assistantai.llm.gemini.model}") String model,
                            @Value("${assistantai.llm.gemini.fallback-model}") String fallbackModel,
                            @Value("${assistantai.llm.gemini.base-url}") String baseUrl,
                            @Value("${assistantai.llm.gemini.timeout-seconds}") long timeoutSeconds) {
        if (apiKey == null || apiKey.isBlank())
            throw new IllegalStateException("GEMINI_API_KEY is not set. Set it or use ASSISTANTAI_LLM_PROVIDER=mock");
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.model = model;
        this.fallbackModel = fallbackModel;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("x-goog-api-key", apiKey)
                .build();
    }

    @Override
    public LlmReply generateReflection(LlmReflectionRequest request) {
        var contents = request.history().stream()
                .map(m -> new GeminiContent(m.getSender() == MessageSender.USER ? "user" : "model",
                        List.of(new GeminiPart(m.getContent()))))
                .toList();
        var structured = generate(GeminiPromptFactory.reflectionSystemInstruction(request.tone(), request.locale()),
                contents, GeminiPromptFactory.reflectionResponseSchema(), 0.7, GeminiStructuredReply.class);
        if (structured.reply() == null || structured.reply().isBlank())
            throw new LlmUnavailableException("Gemini returned an empty reply");
        var distortions = structured.distortions() == null ? List.<LlmDistortion>of()
                : structured.distortions().stream()
                    .map(d -> new LlmDistortion(d.type(), d.evidence(), d.confidence()))
                    .toList();
        return new LlmReply(structured.reply(), structured.emotion(), structured.riskScore(), distortions);
    }

    @Override
    public LlmSummary synthesizeWeeklySummary(LlmSummaryRequest request) {
        var contents = List.of(new GeminiContent("user",
                List.of(new GeminiPart(GeminiPromptFactory.summaryUserPrompt(request)))));
        var structured = generate(GeminiPromptFactory.summarySystemInstruction(request.locale()),
                contents, GeminiPromptFactory.summaryResponseSchema(), 0.3, GeminiStructuredSummary.class);
        if (structured.narrative() == null || structured.narrative().isBlank())
            throw new LlmUnavailableException("Gemini returned an empty summary");
        return new LlmSummary(structured.narrative(),
                structured.keyTriggers() == null ? List.of() : structured.keyTriggers(),
                structured.highlights() == null ? List.of() : structured.highlights());
    }

    private <T> T generate(String systemInstruction, List<GeminiContent> contents,
                           Map<String, Object> schema, double temperature, Class<T> type) {
        var body = new GeminiGenerateContentRequest(
                new GeminiContent(null, List.of(new GeminiPart(systemInstruction))),
                contents,
                new GeminiGenerationConfig(temperature, "application/json", schema));
        var models = model.equals(fallbackModel) ? List.of(model) : List.of(model, fallbackModel);
        for (int i = 0; i < models.size(); i++) {
            try {
                var response = restClient.post()
                        .uri("/v1beta/models/{model}:generateContent", models.get(i))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(GeminiGenerateContentResponse.class);
                return jsonMapper.readValue(extractText(response), type);
            } catch (RestClientResponseException ex) {
                boolean overloaded = ex.getStatusCode().value() == 503 || ex.getStatusCode().value() == 429;
                if (!overloaded || i == models.size() - 1)
                    throw new LlmUnavailableException("Gemini request failed: " + ex.getStatusCode(), ex);
                LOGGER.warn("Gemini model {} unavailable ({}), retrying with {}",
                        models.get(i), ex.getStatusCode().value(), models.get(i + 1));
            } catch (RestClientException | JacksonException ex) {
                throw new LlmUnavailableException("Gemini request failed: " + ex.getMessage(), ex);
            }
        }
        throw new LlmUnavailableException("Gemini request failed");
    }

    private String extractText(GeminiGenerateContentResponse response) {
        if (response == null || response.candidates() == null || response.candidates().isEmpty())
            throw new LlmUnavailableException("Gemini returned no candidates");
        var content = response.candidates().get(0).content();
        if (content == null || content.parts() == null || content.parts().isEmpty()
                || content.parts().get(0).text() == null)
            throw new LlmUnavailableException("Gemini returned no text (finishReason="
                    + response.candidates().get(0).finishReason() + ")");
        return content.parts().get(0).text();
    }
}
