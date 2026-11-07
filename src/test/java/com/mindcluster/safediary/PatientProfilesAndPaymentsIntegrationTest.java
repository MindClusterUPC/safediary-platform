package com.mindcluster.safediary;

import com.mindcluster.safediary.iam.application.internal.outboundservices.federated.FederatedIdentityClient;
import com.mindcluster.safediary.iam.application.internal.outboundservices.notifications.EmailNotificationClient;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.profiles.active=dev",
        "spring.datasource.url=jdbc:h2:mem:patient_profiles_payments_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "iam.jwt.secret=test-secret-with-at-least-32-characters!!",
        "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class PatientProfilesAndPaymentsIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EmailNotificationClient emails;

    @MockitoBean
    private FederatedIdentityClient federated;

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    @DisplayName("Complete test flow: Update Profile, Delete Emergency Contact, Change Email with Password, and Query Payments")
    void testPatientEndpointsFlow() throws Exception {
        String email = "elena.vance@safediary.com";
        String password = "Password123#";

        // 1. IAM: Registra cuenta paciente
        var signUpRes = mvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType("application/json")
                        .content("""
                        {"email":"%s","password":"%s","role":"PATIENT"}
                        """.formatted(email, password)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode session = json.readTree(signUpRes.getResponse().getContentAsString());
        long accountId = session.get("accountId").asLong();
        String token = session.get("accessToken").asString();

        assertThat(accountId).isPositive();
        assertThat(token).isNotBlank();

        // 2. Profiles: Actualizar Nombre y Apellidos (PUT /api/v1/patients/{id})
        mvc.perform(put("/api/v1/patients/" + accountId)
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                        {
                          "firstName": "Elena",
                          "lastName": "Vance"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Elena"))
                .andExpect(jsonPath("$.lastName").value("Vance"))
                .andExpect(jsonPath("$.fullName").value("Elena Vance"));

        // Validaciones: nombre en blanco debe retornar 400 Bad Request
        mvc.perform(put("/api/v1/patients/" + accountId)
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                        {
                          "firstName": "",
                          "lastName": "Vance"
                        }
                        """))
                .andExpect(status().isBadRequest());

        // 3. Profiles: Eliminar Contacto de Emergencia (DELETE /api/v1/patients/{id}/emergency-contact)
        mvc.perform(delete("/api/v1/patients/" + accountId + "/emergency-contact")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emergencyContact").doesNotExist());

        // 4. Profiles: Actualizar Email con Password incorrecto -> 401 UNAUTHORIZED
        mvc.perform(put("/api/v1/patients/" + accountId + "/email")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                        {
                          "newEmail": "nuevo.correo@safeplace.com",
                          "currentPassword": "WrongPassword999#"
                        }
                        """))
                .andExpect(status().isUnauthorized());

        // 5. Profiles: Actualizar Email con Password correcto -> 200 OK
        mvc.perform(put("/api/v1/patients/" + accountId + "/email")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                        {
                          "newEmail": "nuevo.correo@safeplace.com",
                          "currentPassword": "%s"
                        }
                        """.formatted(password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nuevo.correo@safeplace.com"));

        // 6. Payments: Obtener Historial de Transacciones (GET /api/v1/payments/history)
        mvc.perform(get("/api/v1/payments/history?patientAccountId=" + accountId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 7. Payments: Obtener Métodos de Pago (GET /api/v1/payments/methods)
        mvc.perform(get("/api/v1/payments/methods?patientAccountId=" + accountId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // 8. Role Restriction: Un psicólogo no puede acceder a las rutas de paciente
        var psychSignUp = mvc.perform(post("/api/v1/authentication/sign-up")
                        .contentType("application/json")
                        .content("""
                        {"email":"dr.marcus@safediary.com","password":"SafeDoctor2026#","role":"PSYCHOLOGIST"}
                        """))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode psychSession = json.readTree(psychSignUp.getResponse().getContentAsString());
        String psychToken = psychSession.get("accessToken").asString();

        mvc.perform(get("/api/v1/payments/history?patientAccountId=" + accountId)
                        .header("Authorization", "Bearer " + psychToken))
                .andExpect(status().isForbidden());
    }
}
