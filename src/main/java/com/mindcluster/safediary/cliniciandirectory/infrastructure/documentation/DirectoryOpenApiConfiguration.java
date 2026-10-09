package com.mindcluster.safediary.cliniciandirectory.infrastructure.documentation;

import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DirectoryOpenApiConfiguration {
    @Bean public OperationCustomizer directoryDevelopmentHeaders() {
        return (operation, handler) -> {
            var type = handler.getBeanType();
            if (type.getPackageName().equals("com.mindcluster.safediary.cliniciandirectory.interfaces.rest.controllers")
                    && !type.getSimpleName().equals("ClinicianSearchController")) {
                operation.addParametersItem(new Parameter().name("X-Account-Id").in("header")
                        .description("Development identity only; requires dev profile").schema(new StringSchema()).example("1"));
                operation.addParametersItem(new Parameter().name("X-Role").in("header")
                        .description("Development role: PATIENT, PSYCHOLOGIST, ADMIN or MODERATOR").schema(new StringSchema()).example("PSYCHOLOGIST"));
            }
            return operation;
        };
    }
}
