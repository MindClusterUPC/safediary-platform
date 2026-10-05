package com.mindcluster.safediary.assistantai.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables scheduled tasks such as the weekly clinical summary consolidation.
 */
@Configuration
@EnableScheduling
public class AssistantAiSchedulingConfiguration {
}
