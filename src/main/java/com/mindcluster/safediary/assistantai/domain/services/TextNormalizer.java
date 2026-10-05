package com.mindcluster.safediary.assistantai.domain.services;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Normalizes free text for keyword matching: removes accents and lower-cases it.
 */
final class TextNormalizer {

    private TextNormalizer() {
    }

    static String normalize(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
