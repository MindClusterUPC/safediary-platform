package com.mindcluster.safediary.assistantai.domain.model.entities;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.DistortionType;
import lombok.Getter;

/**
 * Maladaptive thinking pattern detected in a user message, with the fragment that evidences it.
 */
@Getter
public class CognitiveDistortion {

    public static final int MAX_EVIDENCE_LENGTH = 300;

    private final Long id;
    private final DistortionType type;
    private final String evidence;
    private final double confidence;

    public CognitiveDistortion(Long id, DistortionType type, String evidence, double confidence) {
        if (type == null) throw new IllegalArgumentException("distortion type must not be null");
        if (confidence < 0.0 || confidence > 1.0)
            throw new IllegalArgumentException("confidence must be between 0 and 1");
        var trimmed = evidence == null ? "" : evidence.trim();
        this.id = id;
        this.type = type;
        this.evidence = trimmed.length() > MAX_EVIDENCE_LENGTH ? trimmed.substring(0, MAX_EVIDENCE_LENGTH) : trimmed;
        this.confidence = confidence;
    }
}
