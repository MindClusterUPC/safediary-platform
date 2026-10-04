package com.mindcluster.safediary.assistantai.domain.services;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PlutchikEmotionTag;

import java.util.Locale;
import java.util.Map;

/**
 * Normalizes raw emotion labels produced by the language model into Plutchik's wheel tags.
 */
public class EmotionClassifierService {

    private static final Map<String, PlutchikEmotionTag> SYNONYMS = Map.ofEntries(
            Map.entry("SAD", PlutchikEmotionTag.SADNESS),
            Map.entry("TRISTEZA", PlutchikEmotionTag.SADNESS),
            Map.entry("TRISTE", PlutchikEmotionTag.SADNESS),
            Map.entry("MIEDO", PlutchikEmotionTag.FEAR),
            Map.entry("SCARED", PlutchikEmotionTag.FEAR),
            Map.entry("ANXIETY", PlutchikEmotionTag.APPREHENSION),
            Map.entry("ANSIEDAD", PlutchikEmotionTag.APPREHENSION),
            Map.entry("WORRY", PlutchikEmotionTag.APPREHENSION),
            Map.entry("PREOCUPACION", PlutchikEmotionTag.APPREHENSION),
            Map.entry("HAPPY", PlutchikEmotionTag.JOY),
            Map.entry("ALEGRIA", PlutchikEmotionTag.JOY),
            Map.entry("FELIZ", PlutchikEmotionTag.JOY),
            Map.entry("CALM", PlutchikEmotionTag.SERENITY),
            Map.entry("CALMA", PlutchikEmotionTag.SERENITY),
            Map.entry("TRANQUILIDAD", PlutchikEmotionTag.SERENITY),
            Map.entry("ANGER", PlutchikEmotionTag.ANNOYANCE),
            Map.entry("ENOJO", PlutchikEmotionTag.ANNOYANCE),
            Map.entry("IRA", PlutchikEmotionTag.ANNOYANCE),
            Map.entry("FRUSTRATION", PlutchikEmotionTag.ANNOYANCE),
            Map.entry("FRUSTRACION", PlutchikEmotionTag.ANNOYANCE),
            Map.entry("REFLECTIVE", PlutchikEmotionTag.PENSIVENESS),
            Map.entry("NOSTALGIA", PlutchikEmotionTag.PENSIVENESS));

    public PlutchikEmotionTag normalizeToPlutchik(String rawTag) {
        if (rawTag == null || rawTag.isBlank()) return null;
        var key = TextNormalizer.normalize(rawTag).trim().toUpperCase(Locale.ROOT);
        for (var tag : PlutchikEmotionTag.values()) {
            if (tag.name().equals(key)) return tag;
        }
        return SYNONYMS.get(key);
    }
}
