package com.mindcluster.safediary.assistantai.infrastructure.llm.springai;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

/**
 * Turns the Gemini chat model off when no Gemini key is configured, so the backend can start with only
 * the backup provider key (Groq...). Without it, Spring AI fails at startup trying to build the Gemini client.
 * Runs last, after application.properties and config/application.properties are loaded.
 */
public class LlmProviderEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        var chatModel = environment.getProperty("spring.ai.model.chat", "");
        var geminiKey = environment.getProperty("spring.ai.google.genai.api-key", "");
        if ("google-genai".equals(chatModel) && geminiKey.isBlank())
            environment.getPropertySources().addFirst(new MapPropertySource("assistantaiLlmProvider",
                    Map.of("spring.ai.model.chat", "none")));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
