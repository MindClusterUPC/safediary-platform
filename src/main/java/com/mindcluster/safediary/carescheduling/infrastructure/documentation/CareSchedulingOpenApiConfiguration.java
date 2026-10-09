package com.mindcluster.safediary.carescheduling.infrastructure.documentation;

import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CareSchedulingOpenApiConfiguration {
    private static final String CONTROLLERS = "com.mindcluster.safediary.carescheduling.interfaces.rest.controllers";

    @Bean public OperationCustomizer careSchedulingDevelopmentHeaders() {
        return (operation, handler) -> {
            var type = handler.getBeanType();
            if (type.getPackageName().equals(CONTROLLERS) && !type.getSimpleName().equals("DevelopmentPaymentSimulationController")) {
                operation.addParametersItem(new Parameter().name("X-Account-Id").in("header")
                        .description("Development identity only; requires dev profile").schema(new StringSchema()).example("1"));
                operation.addParametersItem(new Parameter().name("X-Role").in("header")
                        .description("Development role: PATIENT or PSYCHOLOGIST").schema(new StringSchema()).example("PATIENT"));
            }
            if (type.getSimpleName().equals("AuthorizedSummaryController")) {
                operation.addParametersItem(new Parameter().name("X-Consent-Ref").in("header")
                        .description("Development consent reference until IAM consents exist").schema(new StringSchema()).example("CONSENT-1"));
            }
            return operation;
        };
    }
}
