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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Anti-corruption layer over Spring AI, so any provider it supports can power the AI companion.
 * <p>
 * The provider is chosen with {@code spring.ai.model.chat} (google-genai, openai, ...); this adapter only
 * depends on Spring AI's {@link ChatModel} abstraction and maps its typed output to the AssistantAI model.
 * </p>
 */
@Component
@ConditionalOnProperty(name = "assistantai.llm.provider", havingValue = "spring-ai")
public class SpringAiLlmAdapter implements AssistantLanguageModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringAiLlmAdapter.class);

    private final ChatClient chatClient;
    private final String fallbackModel;

    public SpringAiLlmAdapter(ChatModel chatModel,
                              @Value("${assistantai.llm.spring-ai.fallback-model:}") String fallbackModel) {
        this.chatClient = ChatClient.create(chatModel);
        this.fallbackModel = fallbackModel;
    }

    @Override
    public LlmReply generateReflection(LlmReflectionRequest request) {
        List<Message> history = request.history().stream()
                .<Message>map(m -> m.getSender() == MessageSender.USER
                        ? new UserMessage(m.getContent())
                        : new AssistantMessage(m.getContent()))
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
        try {
            return request(systemInstruction, messages, temperature, null, type);
        } catch (RuntimeException ex) {
            if (fallbackModel == null || fallbackModel.isBlank())
                throw new LlmUnavailableException("Language model request failed: " + ex.getMessage(), ex);
            LOGGER.warn("Primary model failed ({}), retrying with {}", ex.getClass().getSimpleName(), fallbackModel);
            try {
                return request(systemInstruction, messages, temperature, fallbackModel, type);
            } catch (RuntimeException retryEx) {
                throw new LlmUnavailableException("Language model request failed: " + retryEx.getMessage(), retryEx);
            }
        }
    }

    private <T> T request(String systemInstruction, List<Message> messages, double temperature,
                          String model, Class<T> type) {
        var options = ChatOptions.builder().temperature(temperature);
        if (model != null) options.model(model);
        var result = chatClient.prompt()
                .system(systemInstruction)
                .messages(messages)
                .options(options)
                .call()
                .entity(type);
        if (result == null) throw new LlmUnavailableException("The language model returned no content");
        return result;
    }

    /**
     * Typed reflection produced by the model (Spring AI derives the JSON schema from this record).
     */
    public record StructuredReflection(String reply, String emotion, Double riskScore,
                                       List<StructuredDistortion> distortions) {
    }

    public record StructuredDistortion(String type, String evidence, Double confidence) {
    }

    public record StructuredSummary(String narrative, List<String> keyTriggers, List<String> highlights) {
    }
}
