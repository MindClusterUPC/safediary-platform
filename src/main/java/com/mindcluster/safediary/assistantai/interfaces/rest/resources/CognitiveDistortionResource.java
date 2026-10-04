package com.mindcluster.safediary.assistantai.interfaces.rest.resources;

/**
 * Cognitive distortion detected in a user message.
 */
public record CognitiveDistortionResource(String type, String evidence, double confidence) {
}
