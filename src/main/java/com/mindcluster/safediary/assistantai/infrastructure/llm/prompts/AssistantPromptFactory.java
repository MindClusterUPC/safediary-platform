package com.mindcluster.safediary.assistantai.infrastructure.llm.prompts;

import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.LlmSummaryRequest;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.DistortionType;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PlutchikEmotionTag;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Provider-neutral prompts sent to the language model.
 */
public final class AssistantPromptFactory {

    private AssistantPromptFactory() {
    }

    public static String reflectionSystemInstruction(PersonalityTone tone) {
        return """
                Eres el asistente de SafeDiary, una app de bienestar emocional. Tu rol es escucha activa, \
                validación emocional y preguntas reflexivas breves.
                Reglas obligatorias:
                - No diagnosticas, no nombras trastornos, no recetas medicamentos ni das planes terapéuticos. \
                Si te lo piden, recomiendas hablar con un profesional de salud mental.
                - El campo "reply" va SIEMPRE en el idioma en que está escrito el ÚLTIMO mensaje del usuario \
                (inglés, francés, portugués o cualquier otro), aunque estas instrucciones estén en español. \
                Máximo 120 palabras.
                - Estilo: %s
                Responde SOLO con JSON con estos campos:
                - "reply": tu respuesta al usuario.
                - "emotion": emoción predominante del ÚLTIMO mensaje del usuario, uno de: %s.
                - "riskScore": riesgo de autolesión o suicidio en ese último mensaje, de 0.0 a 1.0. Escala: \
                0.0 sin riesgo; 0.4 malestar significativo; 0.65 desesperanza intensa o ideas pasivas de muerte; \
                0.85 o más ideación suicida o autolesión explícita.
                - "distortions": distorsiones cognitivas CLARAMENTE presentes en ese último mensaje (lista vacía si no hay). \
                Cada una con "type" (uno de: %s), "evidence" (fragmento literal del mensaje, máximo 15 palabras) \
                y "confidence" (0.0 a 1.0).
                """.formatted(toneStyle(tone), names(PlutchikEmotionTag.values()),
                names(DistortionType.values()));
    }

    public static String summarySystemInstruction(String locale) {
        return """
                Redactas un resumen semanal para que un psicólogo prepare su próxima sesión con un paciente de SafeDiary.
                Reglas obligatorias:
                - Escribes en tercera persona ("el paciente"), en %s, con un máximo de 200 palabras.
                - No diagnosticas ni recomiendas tratamientos.
                - No copias frases literales del paciente: parafraseas.
                Responde SOLO con JSON con "narrative" (síntesis), "keyTriggers" (máximo 5 detonantes breves) \
                y "highlights" (máximo 5 observaciones breves).
                """.formatted(languageName(locale));
    }

    public static String summaryUserPrompt(LlmSummaryRequest request) {
        var builder = new StringBuilder("Emociones dominantes: ")
                .append(names(request.dominantEmotions().toArray(Enum[]::new)))
                .append("\nMensajes del paciente (orden cronológico):\n");
        request.userMessages().forEach(message -> builder.append("- ").append(message).append('\n'));
        return builder.toString();
    }

    private static String languageName(String locale) {
        return "en".equals(locale) ? "inglés" : "español";
    }

    private static String toneStyle(PersonalityTone tone) {
        return switch (tone) {
            case EMPATHIC -> "cálido y validante, reconoce lo que siente la persona.";
            case REFLECTIVE -> "haz preguntas abiertas que inviten a reflexionar.";
            case ANALYTICAL -> "ayuda a ordenar pensamientos y detectar patrones, de forma directa.";
            case CALM -> "frases cortas, tono sereno y pausado; sugiere respirar.";
        };
    }

    private static String names(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).collect(Collectors.joining(", "));
    }
}
