package com.mindcluster.safediary;

import com.mindcluster.safediary.iam.application.internal.outboundservices.federated.FederatedIdentityClient;
import com.mindcluster.safediary.iam.application.internal.outboundservices.notifications.EmailNotificationClient;
import com.mindcluster.safediary.payments.interfaces.acl.PaymentsContextFacade;
import com.mindcluster.safediary.profiles.domain.model.valueobjects.EmailAddress;
import com.mindcluster.safediary.profiles.domain.repositories.PatientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.profiles.active=dev",
        "spring.datasource.url=jdbc:h2:mem:iam_payments_profiles_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "iam.jwt.secret=test-secret-with-at-least-32-characters!!",
        "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class IamPaymentsProfilesIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PaymentsContextFacade paymentsContextFacade;

    @Autowired
    private PatientRepository patientRepository;

    @MockitoBean
    private EmailNotificationClient emails;

    @MockitoBean
    private FederatedIdentityClient federated;

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    @DisplayName("Sign up patient in IAM automatically triggers FREE subscription in Payments and default profile in Profiles")
    void testSignUpTriggersPaymentsAndProfiles() throws Exception {
        String email = "andres.integracion@safediary.com";
        String password = "SafeSanctuary2025#";

        // 1. IAM: Registra cuenta paciente
        var result = mvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType("application/json")
                        .content("""
                        {"email":"%s","password":"%s","role":"PATIENT"}
                        """.formatted(email, password)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode session = json.readTree(result.getResponse().getContentAsString());
        long accountId = session.get("accountId").asLong();
        String token = session.get("accessToken").asString();

        assertThat(accountId).isPositive();
        assertThat(token).isNotBlank();

        // 2. Payments: Verifica que Payments provisionó la suscripción FREE automáticamente
        var planStatus = paymentsContextFacade.fetchPatientPlanStatus(accountId);
        assertThat(planStatus).isNotNull();
        assertThat(planStatus.plan()).isEqualTo("FREE");
        assertThat(planStatus.active()).isTrue();

        // 3. Profiles: Verifica que Profiles aprovisionó el perfil de paciente
        var patientOpt = patientRepository.findByEmail(EmailAddress.of(email));
        assertThat(patientOpt).isPresent();
        assertThat(patientOpt.get().getEmail().value()).isEqualTo(email);

        // 4. Modificación de Plan con Pagos: Solicitar cambio a plan FREE o consulta
        mvc.perform(get("/api/v1/payments/subscriptions/current?patientAccountId=" + accountId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("FREE"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
}
