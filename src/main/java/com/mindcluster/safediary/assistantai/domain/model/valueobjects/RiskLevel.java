package com.mindcluster.safediary.assistantai.domain.model.valueobjects;

/**
 * Self-harm risk level detected in a user message. HIGH and CRITICAL trigger the crisis protocol.
 */
public enum RiskLevel {
    LOW,
    MODERATE,
    HIGH,
    CRITICAL
}
