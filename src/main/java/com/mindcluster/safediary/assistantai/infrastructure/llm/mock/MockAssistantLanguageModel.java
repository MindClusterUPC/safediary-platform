package com.mindcluster.safediary.assistantai.infrastructure.llm.mock;

import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Deterministic language model used in tests or when no API key is configured.
 */
@Component
@ConditionalOnProperty(name = "assistantai.llm.provider", havingValue = "mock", matchIfMissing = true)
public class MockAssistantLanguageModel implements AssistantLanguageModel {

    @Override
    public LlmReply generateReflection(LlmReflectionRequest request) {
        return new LlmReply("Gracias por compartir cómo te sientes. ¿Qué crees que hay detrás de esa emoción?",
                "PENSIVENESS", 0.0, List.of());
    }

    @Override
    public LlmSummary synthesizeWeeklySummary(LlmSummaryRequest request) {
        return new LlmSummary("Resumen de prueba generado por el proveedor mock.",
                List.of("Carga académica"),
                List.of("El paciente registró sus emociones durante la semana."));
    }
}
