package com.mindcluster.safediary;

import com.mindcluster.safediary.iam.application.internal.outboundservices.federated.FederatedIdentityClient;
import com.mindcluster.safediary.iam.application.internal.outboundservices.notifications.EmailNotificationClient;
import com.mindcluster.safediary.payments.domain.model.valueobjects.SubscriptionPlan;
import com.mindcluster.safediary.payments.domain.repositories.SubscriptionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.profiles.active=dev",
        "spring.datasource.url=jdbc:h2:mem:payments_profiles_security_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "iam.jwt.secret=test-secret-with-at-least-32-characters!!",
        "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class PaymentsAndProfilesSecurityIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @MockitoBean
    private EmailNotificationClient emails;

    @MockitoBean
    private FederatedIdentityClient federated;

    private final JsonMapper json = JsonMapper.builder().build();

    private JsonNode signUp(String email, String password, String role) throws Exception {
        var res = mvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType("application/json")
                        .content("""
                                {"email":"%s","password":"%s","role":"%s"}
                                """.formatted(email, password, role)))
                .andExpect(status().isCreated())
                .andReturn();
        return json.readTree(res.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("Subscription cancel POST works with JWT while GET stays public, checkout preserves active plan, and /patients/me uses accountId")
    void testSubscriptionFlowsAndAccountIdAlignment() throws Exception {
        // Register two psychologists first so patient accountId != patient profile id
        signUp("psych.one@safediary.com", "SafePsych2026#", "PSYCHOLOGIST");
        signUp("psych.two@safediary.com", "SafePsych2026#", "PSYCHOLOGIST");

        JsonNode patientSession = signUp("mateo.fix@safediary.com", "SafePat2026#", "PATIENT");
        long patientAccountId = patientSession.get("accountId").asLong();
        String patientToken = patientSession.get("accessToken").asString();

        var meRes = mvc.perform(get("/api/v1/patients/me")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andReturn();
        long patientProfileId = json.readTree(meRes.getResponse().getContentAsString()).get("id").asLong();
        assertThat(patientProfileId).isNotEqualTo(patientAccountId);

        // Starting checkout does NOT deactivate or overwrite current FREE ACTIVE subscription
        mvc.perform(post("/api/v1/payments/subscriptions/checkout")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType("application/json")
                        .content("""
                                {"patientAccountId":%d,"plan":"TERRA"}
                                """.formatted(patientAccountId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutRequired").value(true));

        mvc.perform(get("/api/v1/payments/subscriptions/current")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("FREE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.active").value(true));

        // Activate TERRA for patientAccountId and verify /patients/me reflects TERRA despite patientProfileId != patientAccountId
        var sub = subscriptionRepository.findByPatientAccountId(patientAccountId).orElseThrow();
        sub.activateSubscription(SubscriptionPlan.TERRA, Instant.now().plus(30, ChronoUnit.DAYS), "sub_test_1", "cus_test_1");
        subscriptionRepository.save(sub);

        mvc.perform(get("/api/v1/patients/me")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planStatus.plan").value("TERRA"))
                .andExpect(jsonPath("$.planStatus.status").value("ACTIVE"))
                .andExpect(jsonPath("$.planStatus.active").value(true));

        // GET /subscriptions/cancel remains public; POST /subscriptions/cancel requires & accepts JWT
        mvc.perform(get("/api/v1/payments/subscriptions/cancel"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/payments/subscriptions/cancel")
                        .contentType("application/json")
                        .content("""
                                {"patientAccountId":%d}
                                """.formatted(patientAccountId)))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/api/v1/payments/subscriptions/cancel")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType("application/json")
                        .content("""
                                {"patientAccountId":%d}
                                """.formatted(patientAccountId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("FREE"))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // Invalid enum returns 400 VALIDATION_ERROR instead of 500
        mvc.perform(put("/api/v1/patients/" + patientProfileId + "/emergency-contact")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType("application/json")
                        .content("""
                                {"name":"Ana","relationship":"FAMILY","phoneNumber":"+51999999999","email":"ana@example.com"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Psychologist profiles, Stripe Connect, and Consultation Checkout enforce role and ownership")
    void testRoleAndOwnershipGuards() throws Exception {
        JsonNode psych1 = signUp("valeria.guard@safediary.com", "SafePsych2026#", "PSYCHOLOGIST");
        long psych1Id = psych1.get("accountId").asLong();
        String psych1Token = psych1.get("accessToken").asString();

        JsonNode psych2 = signUp("other.psych@safediary.com", "SafePsych2026#", "PSYCHOLOGIST");
        String psych2Token = psych2.get("accessToken").asString();

        JsonNode patient = signUp("carlos.guard@safediary.com", "SafePat2026#", "PATIENT");
        long patientId = patient.get("accountId").asLong();
        String patientToken = patient.get("accessToken").asString();

        // Patient cannot register or modify a psychologist profile
        String createBody = """
                {
                  "firstName":"Valeria","lastName":"Guard","email":"valeria.guard@safediary.com",
                  "costPerSession":120.00,"durationMinutes":50,"currency":"USD",
                  "videoCallEnabled":true,"encryptedChatEnabled":true,
                  "bio":"TCC","categories":["Ansiedad"],"certificateTitles":["Lic."]
                }
                """;
        mvc.perform(post("/api/v1/psychologists")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType("application/json")
                        .content(createBody))
                .andExpect(status().isForbidden());

        // Psychologist cannot register a profile with someone else's email
        mvc.perform(post("/api/v1/psychologists")
                        .header("Authorization", "Bearer " + psych2Token)
                        .contentType("application/json")
                        .content(createBody))
                .andExpect(status().isForbidden());

        // Owner psychologist creates profile
        var createdRes = mvc.perform(post("/api/v1/psychologists")
                        .header("Authorization", "Bearer " + psych1Token)
                        .contentType("application/json")
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();
        long psychProfileId = json.readTree(createdRes.getResponse().getContentAsString()).get("id").asLong();

        // Patient & other psychologist cannot update it
        String updateBody = """
                {
                  "costPerSession":10.00,"durationMinutes":30,"currency":"USD",
                  "videoCallEnabled":true,"encryptedChatEnabled":false,
                  "bio":"Tampered","categories":["Hack"],"certificateTitles":["Fake"]
                }
                """;
        mvc.perform(put("/api/v1/psychologists/" + psychProfileId)
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType("application/json")
                        .content(updateBody))
                .andExpect(status().isForbidden());

        mvc.perform(put("/api/v1/psychologists/" + psychProfileId)
                        .header("Authorization", "Bearer " + psych2Token)
                        .contentType("application/json")
                        .content(updateBody))
                .andExpect(status().isForbidden());

        // Connect accounts restricted to owning PSYCHOLOGIST
        mvc.perform(post("/api/v1/payments/connect/accounts")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType("application/json")
                        .content("""
                                {"psychologistAccountId":%d,"email":"valeria.guard@safediary.com"}
                                """.formatted(psych1Id)))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/payments/connect/accounts")
                        .header("Authorization", "Bearer " + psych1Token)
                        .contentType("application/json")
                        .content("""
                                {"psychologistAccountId":%d,"email":"valeria.guard@safediary.com"}
                                """.formatted(psych1Id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.psychologistAccountId").value(psych1Id));

        mvc.perform(get("/api/v1/payments/connect/accounts/" + psych1Id)
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());

        mvc.perform(get("/api/v1/payments/connect/accounts/" + psych1Id)
                        .header("Authorization", "Bearer " + psych2Token))
                .andExpect(status().isForbidden());

        // Consultation checkout restricted to owning PATIENT
        mvc.perform(post("/api/v1/payments/consultations/checkout")
                        .header("Authorization", "Bearer " + psych1Token)
                        .contentType("application/json")
                        .content("""
                                {"patientAccountId":%d,"psychologistAccountId":%d,"appointmentId":101,"totalAmount":120.00,"platformFee":18.00,"currency":"USD"}
                                """.formatted(patientId, psych1Id)))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/v1/payments/consultations/checkout")
                        .header("Authorization", "Bearer " + patientToken)
                        .contentType("application/json")
                        .content("""
                                {"patientAccountId":%d,"psychologistAccountId":%d,"appointmentId":101,"totalAmount":120.00,"platformFee":18.00,"currency":"USD"}
                                """.formatted(patientId + 99, psych1Id)))
                .andExpect(status().isForbidden());
    }
}
