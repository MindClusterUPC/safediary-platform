package com.mindcluster.safediary.rutines;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:routine_isolation;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "iam.jwt.secret=routine-test-secret-with-at-least-32-characters",
    "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class RoutineAccountIsolationIntegrationTest {
    @Autowired MockMvc mvc;
    private final JsonMapper json = JsonMapper.builder().build();
    record Session(String token, long id) {}
    private JsonNode read(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }
    private Session account() throws Exception {
        var response = read(mvc.perform(post("/api/v1/authentication/sign-up").contentType("application/json")
                .content("{\"email\":\"qa-routine-" + UUID.randomUUID() + "@example.test\",\"password\":\"TestPassword123!\",\"role\":\"PATIENT\"}"))
                .andExpect(status().isCreated()));
        return new Session(response.get("accessToken").asText(), response.get("accountId").asLong());
    }
    private ResultActions as(Session session, MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + session.token()));
    }
    private String payload(long owner) {
        return """
            {"patientId":%d,"title":"Mi pausa","frequencyDays":["MONDAY","FRIDAY"],
             "isNotificationActive":false,"description":"Respirar con calma","time":"18:30","minutes":7,"kind":"BREATHING"}
            """.formatted(owner);
    }
    private String create(Session s) throws Exception {
        return read(as(s, post("/api/v1/daily-routines").contentType("application/json").content(payload(s.id())))
                .andExpect(status().isCreated())).get("id").asText();
    }

    @Test void detailsSurviveReloadEditingCompletionAndDeletion() throws Exception {
        var s = account();
        var id = create(s);
        as(s, get("/api/v1/daily-routines/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.time").value("18:30")).andExpect(jsonPath("$.minutes").value(7))
                .andExpect(jsonPath("$.description").value("Respirar con calma")).andExpect(jsonPath("$.kind").value("BREATHING"));
        as(s, put("/api/v1/daily-routines/" + id).contentType("application/json").content("""
            {"title":"Pausa editada","isNotificationActive":true,"frequencyDays":["SUNDAY"],
             "description":"Conectar con mis sentidos","time":"20:15","minutes":3,"kind":"GROUNDING"}
            """)).andExpect(status().isOk());
        as(s, get("/api/v1/daily-routines/" + id)).andExpect(jsonPath("$.title").value("Pausa editada"))
                .andExpect(jsonPath("$.frequencyDays[0]").value("SUNDAY")).andExpect(jsonPath("$.time").value("20:15"))
                .andExpect(jsonPath("$.minutes").value(3)).andExpect(jsonPath("$.kind").value("GROUNDING"))
                .andExpect(jsonPath("$.notificationStatus").value("ENABLED"));
        var completed = read(as(s, post("/api/v1/daily-routines/" + id + "/completion")).andExpect(status().isOk()));
        assertThat(completed.get("completedOn").asText()).isEqualTo(java.time.LocalDate.now(java.time.ZoneId.of("America/Lima")).toString());
        as(s, post("/api/v1/daily-routines/" + id + "/completion")).andExpect(status().isOk())
                .andExpect(jsonPath("$.completedOn").value(completed.get("completedOn").asText()))
                .andExpect(jsonPath("$.isActive").value(true));
        as(s, delete("/api/v1/daily-routines/" + id)).andExpect(status().isNoContent());
        as(s, get("/api/v1/daily-routines/" + id)).andExpect(status().isNotFound());
        assertThat(read(as(s, get("/api/v1/daily-routines/patient/" + s.id())).andExpect(status().isOk())).size()).isZero();
    }

    @Test void otherAccountsCannotListReadUpdateCompleteOrDeleteRoutines() throws Exception {
        var a = account(); var b = account(); var id = create(a);
        mvc.perform(get("/api/v1/daily-routines/" + id)).andExpect(status().isUnauthorized());
        assertThat(read(as(b, get("/api/v1/daily-routines/patient/" + b.id())).andExpect(status().isOk())).size()).isZero();
        as(b, get("/api/v1/daily-routines/patient/" + a.id())).andExpect(status().isForbidden());
        as(b, post("/api/v1/daily-routines").contentType("application/json").content(payload(a.id()))).andExpect(status().isForbidden());
        as(b, get("/api/v1/daily-routines/" + id)).andExpect(status().isForbidden());
        as(b, put("/api/v1/daily-routines/" + id).contentType("application/json").content(payload(b.id()))).andExpect(status().isForbidden());
        as(b, delete("/api/v1/daily-routines/" + id)).andExpect(status().isForbidden());
        as(b, post("/api/v1/daily-routines/" + id + "/completion")).andExpect(status().isForbidden());
        as(b, patch("/api/v1/daily-routines/" + id + "/notification").contentType("application/json").content("{\"isEnabled\":true}")).andExpect(status().isForbidden());
        as(b, patch("/api/v1/daily-routines/" + id + "/toggle-active")).andExpect(status().isForbidden());
        as(a, get("/api/v1/daily-routines/" + id)).andExpect(status().isOk());
    }

    @Test void invalidTimeDurationAndEmptyDaysAreRejected() throws Exception {
        var s = account();
        for (var body : new String[] {payload(s.id()).replace("18:30", "25:30"), payload(s.id()).replace("\"minutes\":7", "\"minutes\":0"),
                payload(s.id()).replace("[\"MONDAY\",\"FRIDAY\"]", "[]")}) {
            as(s, post("/api/v1/daily-routines").contentType("application/json").content(body)).andExpect(status().isBadRequest());
        }
    }
}
