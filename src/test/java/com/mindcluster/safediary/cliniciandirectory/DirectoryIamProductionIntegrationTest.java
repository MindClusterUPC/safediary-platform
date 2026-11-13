package com.mindcluster.safediary.cliniciandirectory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.profiles.active=prod",
        "spring.datasource.url=jdbc:h2:mem:directory_iam_prod;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "iam.jwt.secret=directory-production-test-secret-at-least-32-characters",
        "clinician-directory.demo-identity.enabled=true",
        "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class DirectoryIamProductionIntegrationTest {
    private static final String PROFILE = """
            {"displayName":"Demo - Ana Torres","professionalTitle":"Psicologia clinica",
             "bio":"Perfil ficticio de prueba","specialties":["Ansiedad"],
             "amount":80,"currency":"PEN","durationMinutes":50}
            """;
    @Autowired MockMvc mvc;
    private final JsonMapper json = JsonMapper.builder().build();

    private MockHttpServletRequestBuilder body(MockHttpServletRequestBuilder request, String content) {
        return request.contentType("application/json").content(content);
    }

    private String register(String email, String role) throws Exception {
        var response = mvc.perform(body(post("/api/v1/authentication/sign-up"), """
                {"email":"%s","password":"DirectoryTest2026!","role":"%s"}
                """.formatted(email, role))).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(response).get("accessToken").asString();
    }

    @Test void realTokensAllowProfessionalProfilesInProductionAndPreserveOwnership() throws Exception {
        var owner = register("owner@example.test", "PSYCHOLOGIST");
        var other = register("other@example.test", "PSYCHOLOGIST");
        var patient = register("patient@example.test", "PATIENT");

        mvc.perform(get("/api/v1/clinicians/me").header("Authorization", "Bearer " + owner))
                .andExpect(status().isNotFound());
        var created = mvc.perform(body(post("/api/v1/clinicians"), PROFILE)
                        .header("Authorization", "Bearer " + owner))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.publicationStatus").value("DRAFT"))
                .andReturn().getResponse().getContentAsString();
        var id = json.readTree(created).get("id").asLong();

        mvc.perform(get("/api/v1/clinicians/me").header("Authorization", "Bearer " + owner)
                        .header("X-Account-Id", "999").header("X-Role", "ADMIN"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(body(put("/api/v1/clinicians/" + id), PROFILE)
                        .header("Authorization", "Bearer " + owner))
                .andExpect(status().isOk());
        mvc.perform(body(put("/api/v1/clinicians/" + id), PROFILE)
                        .header("Authorization", "Bearer " + other))
                .andExpect(status().isForbidden());
        mvc.perform(body(post("/api/v1/clinicians"), PROFILE)
                        .header("Authorization", "Bearer " + patient).header("X-Role", "PSYCHOLOGIST"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/clinicians/" + id + "/publication")
                        .header("Authorization", "Bearer " + owner))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/clinicians/" + id)).andExpect(status().isNotFound());
    }

    @Test void productionNeverUsesForgedHeadersOrInvalidTokens() throws Exception {
        mvc.perform(get("/api/v1/clinicians/me").header("X-Account-Id", "1").header("X-Role", "ADMIN"))
                .andExpect(status().isUnauthorized());
        mvc.perform(body(post("/api/v1/clinicians"), PROFILE)
                        .header("X-Account-Id", "1").header("X-Role", "PSYCHOLOGIST"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/clinicians/me").header("Authorization", "Bearer invalid")
                        .header("X-Account-Id", "1").header("X-Role", "ADMIN"))
                .andExpect(status().isUnauthorized());
    }
}
