package com.mindcluster.safediary.assistantai.domain.model.events;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.DistortionType;
import java.time.Instant;

/**
 * Raised when a cognitive distortion is detected in a user message.
 */
public record CognitiveDistortionDetectedEvent(Long sessionId, DistortionType type, Instant occurredAt) {
}
