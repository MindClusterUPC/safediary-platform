package com.mindcluster.safediary.assistantai.infrastructure.configuration;

import com.mindcluster.safediary.assistantai.domain.services.ClinicalSummarySynthesizerService;
import com.mindcluster.safediary.assistantai.domain.services.CognitiveDistortionService;
import com.mindcluster.safediary.assistantai.domain.services.EmotionClassifierService;
import com.mindcluster.safediary.assistantai.domain.services.RiskPolicyService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the AssistantAI domain services as beans, keeping the domain layer free of Spring.
 */
@Configuration
public class AssistantAiDomainServicesConfiguration {

    @Bean
    public RiskPolicyService riskPolicyService() {
        return new RiskPolicyService();
    }

    @Bean
    public EmotionClassifierService emotionClassifierService() {
        return new EmotionClassifierService();
    }

    @Bean
    public CognitiveDistortionService cognitiveDistortionService() {
        return new CognitiveDistortionService();
    }

    @Bean
    public ClinicalSummarySynthesizerService clinicalSummarySynthesizerService() {
        return new ClinicalSummarySynthesizerService();
    }
}
