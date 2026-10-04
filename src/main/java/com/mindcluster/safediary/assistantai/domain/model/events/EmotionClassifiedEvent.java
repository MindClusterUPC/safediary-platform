package com.mindcluster.safediary.assistantai.domain.model.events;

import com.mindcluster.safediary.assistantai.domain.model.valueobjects.PlutchikEmotionTag;
import java.time.Instant;

/**
 * Raised when the predominant emotion of a user message is classified.
 */
public record EmotionClassifiedEvent(Long sessionId, PlutchikEmotionTag emotion, Instant occurredAt) {
}
