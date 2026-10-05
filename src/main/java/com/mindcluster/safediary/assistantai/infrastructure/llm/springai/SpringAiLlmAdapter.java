package com.mindcluster.safediary.assistantai.infrastructure.llm.springai;

import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.*;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.MessageSender;
import com.mindcluster.safediary.assistantai.infrastructure.llm.prompts.AssistantPromptFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Anti-corruption layer over Spring AI, so any provider it supports can power the AI companion.
 * <p>
 * The provider is chosen with {@code spring.ai.model.chat} (google-genai, openai, ...); this adapter only
 * depends on Spring AI's {@link ChatModel} abstraction and maps its typed output to the AssistantAI model.
 * </p>
 * <p>
 * Models are tried in order: the primary model, {@code assistantai.llm.spring-ai.fallback-model} on the same
 * provider, and finally the OpenAI-compatible backup provider (Groq, OpenRouter...) when
 * {@code assistantai.llm.backup.api-key} is set.
 * </p>
 */
@Component
@ConditionalOnProperty(name = "assistantai.llm.provider", havingValue = "spring-ai")
public class SpringAiLlmAdapter implements AssistantLanguageModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringAiLlmAdapter.class);

    private final List<Route> routes = new ArrayList<>();

    public SpringAiLlmAdapter(ChatModel chatModel,
                              @Value("${assistantai.llm.spring-ai.fallback-model:}") String fallbackModel,
                              @Value("${assistantai.llm.backup.api-key:}") String backupApiKey,
                              @Value("${assistantai.llm.backup.base-url:}") String backupBaseUrl,
                              @Value("${assistantai.llm.backup.model:}") String backupModel,
                              @Value("${assistantai.llm.backup.timeout-seconds:30}") long backupTimeoutSeconds) {
        var primary = ChatClient.create(chatModel);
        routes.add(new Route("primary", primary, null));
        if (!fallbackModel.isBlank())
            routes.add(new Route(fallbackModel, primary, fallbackModel));
        if (!backupApiKey.isBlank()) {
            var backup = OpenAiChatModel.builder()
                    .options(OpenAiChatOptions.builder()
                            .baseUrl(backupBaseUrl)
                            .apiKey(backupApiKey)
                            .model(backupModel)
                            .timeout(Duration.ofSeconds(backupTimeoutSeconds))
                            .maxRetries(0)
                            .build())
                    .build();
            routes.add(new Route("backup " + backupModel, ChatClient.create(backup), null));
        }
    }

    @Override
    public LlmReply generateReflection(LlmReflectionRequest request) {
        var messages = request.history();
        var lastIndex = messages.size() - 1;
        List<Message> history = java.util.stream.IntStream.range(0, messages.size())
                .<Message>mapToObj(i -> {
                    var m = messages.get(i);
                    if (m.getSender() != MessageSender.USER) return new AssistantMessage(m.getContent());
                    // Smaller fallback models drift to English; restating the rule next to the message keeps them on track.
                    return new UserMessage(i == lastIndex
                            ? m.getContent() + AssistantPromptFactory.REPLY_LANGUAGE_REMINDER
                            : m.getContent());
                })
                .toList();
        var structured = call(AssistantPromptFactory.reflectionSystemInstruction(request.tone()),
                history, 0.7, StructuredReflection.class);
        if (structured.reply() == null || structured.reply().isBlank())
            throw new LlmUnavailableException("The language model returned an empty reply");
        var distortions = structured.distortions() == null ? List.<LlmDistortion>of()
                : structured.distortions().stream()
                    .map(d -> new LlmDistortion(d.type(), d.evidence(), d.confidence()))
                    .toList();
        return new LlmReply(structured.reply(), structured.emotion(), structured.riskScore(), distortions);
    }

    @Override
    public LlmSummary synthesizeWeeklySummary(LlmSummaryRequest request) {
        var structured = call(AssistantPromptFactory.summarySystemInstruction(request.locale()),
                List.of(new UserMessage(AssistantPromptFactory.summaryUserPrompt(request))), 0.3, StructuredSummary.class);
        if (structured.narrative() == null || structured.narrative().isBlank())
            throw new LlmUnavailableException("The language model returned an empty summary");
        return new LlmSummary(structured.narrative(),
                structured.keyTriggers() == null ? List.of() : structured.keyTriggers(),
                structured.highlights() == null ? List.of() : structured.highlights());
    }

    private <T> T call(String systemInstruction, List<Message> messages, double temperature, Class<T> type) {
        RuntimeException lastFailure = null;
        for (var route : routes) {
            try {
                return request(route, systemInstruction, messages, temperature, type);
            } catch (RuntimeException ex) {
                LOGGER.warn("Language model '{}' failed ({}), trying the next one", route.name(), ex.getClass().getSimpleName());
                lastFailure = ex;
            }
        }
        throw new LlmUnavailableException("Language model request failed: " + lastFailure.getMessage(), lastFailure);
    }

    private <T> T request(Route route, String systemInstruction, List<Message> messages, double temperature,
                          Class<T> type) {
        var options = ChatOptions.builder().temperature(temperature);
        if (route.model() != null) options.model(route.model());
        var result = route.client().prompt()
                .system(systemInstruction)
                .messages(messages)
                .options(options)
                .call()
                .entity(type);
        if (result == null) throw new LlmUnavailableException("The language model returned no content");
        return result;
    }

    private record Route(String name, ChatClient client, String model) {
    }

    /**
     * Typed reflection produced by the model (Spring AI derives the JSON schema from this record).
     */
    public record StructuredReflection(String language, String reply, String emotion, Double riskScore,
                                       List<StructuredDistortion> distortions) {
    }

    public record StructuredDistortion(String type, String evidence, Double confidence) {
    }

    public record StructuredSummary(String narrative, List<String> keyTriggers, List<String> highlights) {
    }
}
