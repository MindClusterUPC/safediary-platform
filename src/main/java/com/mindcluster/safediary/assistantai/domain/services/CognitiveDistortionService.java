package com.mindcluster.safediary.assistantai.domain.services;

import com.mindcluster.safediary.assistantai.domain.model.entities.CognitiveDistortion;
import com.mindcluster.safediary.assistantai.domain.model.valueobjects.DistortionType;

import java.util.Locale;
import java.util.Optional;

/**
 * Turns raw distortion detections from the language model into domain distortions, keeping only reliable ones.
 */
public class CognitiveDistortionService {

    public static final double MIN_CONFIDENCE = 0.6;

    public Optional<CognitiveDistortion> toReliableDistortion(String rawType, String evidence, Double confidence) {
        if (rawType == null || confidence == null || confidence < MIN_CONFIDENCE || confidence > 1.0)
            return Optional.empty();
        try {
            var type = DistortionType.valueOf(rawType.trim().toUpperCase(Locale.ROOT));
            return Optional.of(new CognitiveDistortion(null, type, evidence, confidence));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
