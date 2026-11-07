package com.mindcluster.safediary.iam;

import com.mindcluster.safediary.iam.application.internal.outboundservices.federated.FederatedIdentityClient;
import com.mindcluster.safediary.iam.application.internal.outboundservices.federated.FederatedIdentityClient.FederatedIdentity;
import com.mindcluster.safediary.iam.application.internal.outboundservices.notifications.EmailNotificationClient;
import com.mindcluster.safediary.iam.domain.model.valueobjects.AuthProviderType;
import com.mindcluster.safediary.iam.interfaces.acl.IamContextFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={
        "spring.profiles.active=dev",
        "spring.datasource.url=jdbc:h2:mem:iam_api;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "clinician-directory.demo-identity.enabled=true",
        "iam.jwt.secret=test-secret-with-at-least-32-characters!!",
        "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class IamApiIntegrationTest {
    private static final String PASSWORD = "SafeSanctuary2025#";
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired IamContextFacade facade;
    @MockitoBean EmailNotificationClient emails;
    @MockitoBean FederatedIdentityClient federated;
    private final JsonMapper json = JsonMapper.builder().build();

    @BeforeEach void clean() {
        for (String table : new String[]{"audit_events", "sharing_permissions", "consents", "vault_entries", "private_vaults",
                "refresh_tokens", "password_reset_tokens", "auth_providers", "accounts"})
            jdbc.update("delete from iam." + table);
    }

    private MockHttpServletRequestBuilder body(MockHttpServletRequestBuilder request, String content) {
        return request.contentType("application/json").content(content);
    }
    private JsonNode read(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }
    private ResultActions as(MockHttpServletRequestBuilder request, String token) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + token));
    }
    private JsonNode signUp(String email, String role) throws Exception {
        return read(mvc.perform(body(post("/api/v1/authentication/sign-up"), """
            {"email":"%s","password":"%s","role":"%s"}
            """.formatted(email, PASSWORD, role))).andExpect(status().isCreated()));
    }
    private ResultActions signIn(String email, String password) throws Exception {
        return mvc.perform(body(post("/api/v1/authentication/sign-in"), """
            {"email":"%s","password":"%s"}
            """.formatted(email, password)));
    }

    @Test void signUpSignInAndAccountLockout() throws Exception {
        var session = signUp("maria@safeplace.com", "PATIENT");
        assertThat(session.get("tokenType").asString()).isEqualTo("Bearer");
        mvc.perform(body(post("/api/v1/authentication/sign-up"), """
            {"email":"MARIA@safeplace.com","password":"%s"}
            """.formatted(PASSWORD))).andExpect(status().isConflict());
        mvc.perform(body(post("/api/v1/authentication/sign-up"), """
            {"email":"elena@safeplace.com","password":"onlyletters"}
            """)).andExpect(status().isBadRequest());
        mvc.perform(body(post("/api/v1/authentication/sign-up"), """
            {"email":"boss@safeplace.com","password":"%s","role":"ADMIN"}
            """.formatted(PASSWORD))).andExpect(status().isBadRequest());

        as(get("/api/v1/accounts/me"), session.get("accessToken").asString()).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("maria@safeplace.com")).andExpect(jsonPath("$.role").value("PATIENT"));
        mvc.perform(get("/api/v1/accounts/me")).andExpect(status().isUnauthorized());
        as(get("/api/v1/accounts/me"), "not-a-token").andExpect(status().isUnauthorized());

        for (int i = 0; i < 5; i++) signIn("maria@safeplace.com", "Wrong-password1").andExpect(status().isUnauthorized());
        signIn("maria@safeplace.com", PASSWORD).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("try again after")));
        jdbc.update("update iam.accounts set locked_until = null");
        signIn("maria@safeplace.com", PASSWORD).andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").exists());
    }

    @Test void bearerTokenAuthenticatesOtherBoundedContextsWithoutDevelopmentHeaders() throws Exception {
        var token = signUp("laura@psicosalud.org", "PSYCHOLOGIST").get("accessToken").asString();
        var profile = """
            {"displayName":"Dra. Laura Gomez","professionalTitle":"Psicologa clinica","bio":"Ansiedad y estres academico",
             "specialties":["Ansiedad"],"amount":120.00,"currency":"PEN","durationMinutes":50}
            """;
        as(body(post("/api/v1/clinicians"), profile), token).andExpect(status().isCreated());
        mvc.perform(get("/api/v1/clinicians/me").header("Authorization", "Bearer " + token)
                        .header("X-Account-Id", "999").header("X-Role", "ADMIN"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.displayName").value("Dra. Laura Gomez"));
        mvc.perform(get("/api/v1/clinicians")).andExpect(status().isOk());
    }

    @Test void passwordRecoveryWithSixDigitCodeNeverRevealsTheAccount() throws Exception {
        signUp("maria@safeplace.com", "PATIENT");
        mvc.perform(body(post("/api/v1/authentication/password-reset-requests"), "{\"email\":\"nobody@safeplace.com\"}"))
                .andExpect(status().isAccepted());
        verifyNoInteractions(emails);
        mvc.perform(body(post("/api/v1/authentication/password-reset-requests"), "{\"email\":\"maria@safeplace.com\"}"))
                .andExpect(status().isAccepted());
        var code = ArgumentCaptor.forClass(String.class);
        verify(emails).sendPasswordResetCode(eq("maria@safeplace.com"), code.capture(), any(Instant.class));
        var wrong = code.getValue().equals("000000") ? "111111" : "000000";

        mvc.perform(body(post("/api/v1/authentication/password-reset-requests/verification"), """
            {"email":"maria@safeplace.com","code":"%s"}
            """.formatted(wrong))).andExpect(status().is(422));
        mvc.perform(body(post("/api/v1/authentication/password-reset-requests/verification"), """
            {"email":"maria@safeplace.com","code":"%s"}
            """.formatted(code.getValue()))).andExpect(status().isOk());
        mvc.perform(body(post("/api/v1/authentication/password-resets"), """
            {"email":"maria@safeplace.com","code":"%s","newPassword":"%s"}
            """.formatted(code.getValue(), PASSWORD))).andExpect(status().isBadRequest());
        mvc.perform(body(post("/api/v1/authentication/password-resets"), """
            {"email":"maria@safeplace.com","code":"%s","newPassword":"NewSanctuary2026!"}
            """.formatted(code.getValue()))).andExpect(status().isOk());
        mvc.perform(body(post("/api/v1/authentication/password-resets"), """
            {"email":"maria@safeplace.com","code":"%s","newPassword":"Another2027!"}
            """.formatted(code.getValue()))).andExpect(status().is(422));
        signIn("maria@safeplace.com", PASSWORD).andExpect(status().isUnauthorized());
        signIn("maria@safeplace.com", "NewSanctuary2026!").andExpect(status().isOk());
    }

    @Test void consentGrantRevocationAndAuditTrail() throws Exception {
        var patient = signUp("maria@safeplace.com", "PATIENT");
        var specialist = signUp("laura@psicosalud.org", "PSYCHOLOGIST");
        var patientToken = patient.get("accessToken").asString();
        long patientId = patient.get("accountId").asLong(), specialistId = specialist.get("accountId").asLong();
        var grant = """
            {"granteeAccountId":%d,"permissions":[{"scope":"AI_CLINICAL_SUMMARIES","periodStart":"2026-10-01","periodEnd":"2026-10-31"}]}
            """.formatted(specialistId);

        as(body(post("/api/v1/consents"), grant), specialist.get("accessToken").asString()).andExpect(status().isForbidden());
        long consentId = read(as(body(post("/api/v1/consents"), grant), patientToken).andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))).get("id").asLong();
        as(body(post("/api/v1/consents"), grant), patientToken).andExpect(status().isConflict());
        as(get("/api/v1/consent-scopes"), specialist.get("accessToken").asString())
                .andExpect(jsonPath("$[0].permissions[0].scope").value("AI_CLINICAL_SUMMARIES"));

        assertThat(facade.authorizeConsentedAccess(patientId, specialistId, "AI_CLINICAL_SUMMARIES", LocalDate.of(2026, 10, 9),
                "care_scheduling", "APPOINTMENT", 7L)).contains(consentId);
        assertThat(facade.authorizeConsentedAccess(patientId, specialistId, "DIARY_ENTRIES", LocalDate.of(2026, 10, 9),
                "diary", "DIARY_ENTRY", 3L)).isEmpty();

        as(post("/api/v1/consents/" + consentId + "/revocation"), patientToken).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"));
        as(get("/api/v1/consents"), patientToken).andExpect(content().json("[]"));
        assertThat(facade.authorizeConsentedAccess(patientId, specialistId, "AI_CLINICAL_SUMMARIES", null,
                "care_scheduling", "APPOINTMENT", 7L)).isEmpty();
        as(get("/api/v1/audit-events"), patientToken).andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[*].action", org.hamcrest.Matchers.hasItems(
                        "CONSENT_GRANTED", "ACCESS_GRANTED", "CONSENT_REVOKED", "ACCESS_DENIED")));
    }

    @Test void biometricUnlockAndAccountDeletion() throws Exception {
        var token = signUp("maria@safeplace.com", "PATIENT").get("accessToken").asString();
        var unlockToken = read(as(post("/api/v1/accounts/me/biometric-unlock"), token).andExpect(status().isCreated()))
                .get("unlockToken").asString();
        mvc.perform(body(post("/api/v1/authentication/biometric-sign-in"), "{\"unlockToken\":\"%s\"}".formatted(unlockToken)))
                .andExpect(status().isOk());
        mvc.perform(body(post("/api/v1/authentication/biometric-sign-in"), "{\"unlockToken\":\"forged\"}"))
                .andExpect(status().isUnauthorized());

        as(body(post("/api/v1/accounts/me/deletion-request"), "{\"password\":\"Wrong-password1\"}"), token)
                .andExpect(status().isUnauthorized());
        as(body(post("/api/v1/accounts/me/deletion-request"), "{\"password\":\"%s\"}".formatted(PASSWORD)), token)
                .andExpect(status().isAccepted()).andExpect(jsonPath("$.status").value("PENDING_DELETION"))
                .andExpect(jsonPath("$.deletionScheduledAt").exists());
        as(get("/api/v1/accounts/me"), token).andExpect(status().isUnauthorized());
        mvc.perform(body(post("/api/v1/authentication/biometric-sign-in"), "{\"unlockToken\":\"%s\"}".formatted(unlockToken)))
                .andExpect(status().isUnauthorized());
        signIn("maria@safeplace.com", PASSWORD).andExpect(status().isUnauthorized());
    }

    @Test void privateVaultProtectsEntriesAndLocksAfterFiveWrongPins() throws Exception {
        var token = signUp("maria@safeplace.com", "PATIENT").get("accessToken").asString();
        as(body(post("/api/v1/vault"), "{\"pin\":\"4821\"}"), token).andExpect(status().isCreated());
        as(body(post("/api/v1/vault"), "{\"pin\":\"4821\"}"), token).andExpect(status().isConflict());
        as(body(post("/api/v1/vault/entries"), "{\"pin\":\"4821\",\"diaryEntryId\":15}"), token).andExpect(status().isOk())
                .andExpect(jsonPath("$.protectedEntryIds[0]").value(15));
        for (int i = 0; i < 5; i++)
            as(body(post("/api/v1/vault/access"), "{\"pin\":\"0000\"}"), token).andExpect(status().isForbidden());
        as(body(post("/api/v1/vault/access"), "{\"pin\":\"4821\"}"), token).andExpect(status().is(422))
                .andExpect(jsonPath("$.details").value(org.hamcrest.Matchers.containsString("try again after")));
    }

    @Test void googleSignInCreatesTheAccountOnceAndRecoversItLater() throws Exception {
        when(federated.verify(eq(AuthProviderType.GOOGLE), eq("valid-google-token")))
                .thenReturn(Optional.of(new FederatedIdentity("google-sub-1", "elena@gmail.com", true)));
        var request = "{\"provider\":\"GOOGLE\",\"idToken\":\"valid-google-token\"}";
        long first = read(mvc.perform(body(post("/api/v1/authentication/federated-sign-in"), request)).andExpect(status().isOk()))
                .get("accountId").asLong();
        long second = read(mvc.perform(body(post("/api/v1/authentication/federated-sign-in"), request)).andExpect(status().isOk()))
                .get("accountId").asLong();
        assertThat(second).isEqualTo(first);
        mvc.perform(body(post("/api/v1/authentication/federated-sign-in"), "{\"provider\":\"GOOGLE\",\"idToken\":\"forged\"}"))
                .andExpect(status().isUnauthorized());
    }
}
