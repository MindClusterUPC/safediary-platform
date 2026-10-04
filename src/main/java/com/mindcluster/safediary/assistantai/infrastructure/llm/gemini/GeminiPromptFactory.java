package com.mindcluster.safediary.assistantai.infrastructure.llm.gemini;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.DistortionType;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PersonalityTone;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PlutchikEmotionTag;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds the prompts and JSON schemas sent to Gemini.
 */
public final class GeminiPromptFactory {

    private GeminiPromptFactory() {
    }

    public static String reflectionSystemInstruction(PersonalityTone tone, String locale) {
        return """
                Eres el asistente de SafeDiary, una app de bienestar emocional. Tu rol es escucha activa, \
                validación emocional y preguntas reflexivas breves.
                Reglas obligatorias:
                - No diagnosticas, no nombras trastornos, no recetas medicamentos ni das planes terapéuticos. \
                Si te lo piden, recomiendas hablar con un profesional de salud mental.
                - Respondes en %s, con un máximo de 120 palabras.
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
                """.formatted(languageName(locale), toneStyle(tone), names(PlutchikEmotionTag.values()),
                names(DistortionType.values()));
    }

    public static Map<String, Object> reflectionResponseSchema() {
        var distortionItem = Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "type", Map.of("type", "STRING", "enum", enumNames(DistortionType.values())),
                        "evidence", Map.of("type", "STRING"),
                        "confidence", Map.of("type", "NUMBER")),
                "required", List.of("type", "evidence", "confidence"));
        return Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "reply", Map.of("type", "STRING"),
                        "emotion", Map.of("type", "STRING", "enum", enumNames(PlutchikEmotionTag.values())),
                        "riskScore", Map.of("type", "NUMBER"),
                        "distortions", Map.of("type", "ARRAY", "items", distortionItem)),
                "required", List.of("reply", "emotion", "riskScore", "distortions"));
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

    private static List<String> enumNames(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).toList();
    }

    private static String names(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).collect(Collectors.joining(", "));
    }
}
