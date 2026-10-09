package com.mindcluster.safediary.carescheduling;

import com.mindcluster.safediary.carescheduling.application.commandservices.CareSchedulingCommandService;
import com.mindcluster.safediary.carescheduling.domain.model.commands.ExpireSlotHoldsCommand;
import com.mindcluster.safediary.carescheduling.interfaces.acl.CareSchedulingContextFacade;
import com.mindcluster.safediary.carescheduling.interfaces.acl.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={
        "spring.profiles.active=dev",
        "spring.datasource.url=jdbc:h2:mem:care_scheduling_api;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "clinician-directory.demo-identity.enabled=true",
        "care-scheduling.demo-identity.enabled=true",
        "care-scheduling.payment-simulation.enabled=true",
        "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
@RecordApplicationEvents
class CareSchedulingApiIntegrationTest {
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final long PSYCHOLOGIST = 10, PATIENT = 20, OTHER_PATIENT = 21;
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired CareSchedulingContextFacade facade;
    @Autowired CareSchedulingCommandService commands;
    @Autowired ApplicationEvents events;
    private final JsonMapper json = JsonMapper.builder().build();
    private final LocalDate day = LocalDate.now(LIMA).plusDays(2);

    @BeforeEach void clean() {
        for (String table : new String[]{"summary_access_audits", "clinical_sessions", "slot_holds", "appointments",
                "availability_slots", "coordination_messages", "contact_requests"})
            jdbc.update("delete from care_scheduling." + table);
        for (String table : new String[]{"review_helpful_votes", "review_reports", "reviews", "verification_requests",
                "rating_summaries", "trust_scores", "completed_sessions", "consultation_rates", "clinician_specialties", "clinician_profiles"})
            jdbc.update("delete from clinician_directory." + table);
    }

    private ResultActions as(MockHttpServletRequestBuilder request, long account, String role) throws Exception {
        return mvc.perform(request.header("X-Account-Id", account).header("X-Role", role));
    }
    private MockHttpServletRequestBuilder body(MockHttpServletRequestBuilder request, String content) {
        return request.contentType("application/json").content(content);
    }
    private JsonNode read(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }
    private Instant at(int hour) { return day.atTime(hour, 0).atZone(LIMA).toInstant(); }

    /** Publishes a verified clinician (S/ 120.00, 50 minutes) through the Clinician Directory API. */
    private long publishedClinician() throws Exception {
        long clinician = read(as(body(post("/api/v1/clinicians"), """
            {"displayName":"Dra. Laura Gomez","professionalTitle":"Psicologa clinica","bio":"Ansiedad y estres academico",
             "specialties":["Ansiedad","TCC"],"amount":120.00,"currency":"PEN","durationMinutes":50}
            """), PSYCHOLOGIST, "PSYCHOLOGIST").andExpect(status().isCreated())).get("id").asLong();
        long verification = read(as(body(post("/api/v1/clinicians/" + clinician + "/verification-requests"), """
            {"licenseNumber":"CPP-1234","specialty":"Ansiedad","documentRef":"private://credentials/license.pdf"}
            """), PSYCHOLOGIST, "PSYCHOLOGIST").andExpect(status().isCreated())).get("id").asLong();
        as(body(patch("/api/v1/verification-requests/" + verification + "/decision"), """
            {"decision":"APPROVED"}
            """), 900, "ADMIN").andExpect(status().isOk());
        as(post("/api/v1/clinicians/" + clinician + "/publication"), PSYCHOLOGIST, "PSYCHOLOGIST").andExpect(status().isOk());
        as(body(put("/api/v1/schedule/availability"), """
            [{"dayOfWeek":"%s","startTime":"09:00","endTime":"21:00"}]
            """.formatted(day.getDayOfWeek())), PSYCHOLOGIST, "PSYCHOLOGIST").andExpect(status().isOk());
        return clinician;
    }
    private long contact(long clinician, long patient) throws Exception {
        return read(as(body(post("/api/v1/contact-requests"), """
            {"clinicianId":%d,"message":"Hola, quisiera agendar una sesion sobre ansiedad en examenes"}
            """.formatted(clinician)), patient, "PATIENT").andExpect(status().isCreated())).get("id").asLong();
    }
    private ResultActions propose(long contactRequest, Instant startsAt) throws Exception {
        return as(body(post("/api/v1/schedule/proposals"), """
            {"contactRequestId":%d,"startsAt":"%s"}
            """.formatted(contactRequest, startsAt)), PSYCHOLOGIST, "PSYCHOLOGIST");
    }
    /** Returns the payment reference requested to Payments when the patient accepts. */
    private JsonNode held(long clinician, long patient, int hour) throws Exception {
        long proposal = read(propose(contact(clinician, patient), at(hour)).andExpect(status().isCreated())).get("id").asLong();
        return read(as(post("/api/v1/schedule/proposals/" + proposal + "/acceptance"), patient, "PATIENT")
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("HELD")));
    }
    private ResultActions pay(JsonNode appointment, boolean approved) throws Exception {
        return mvc.perform(body(post("/api/v1/dev/care-scheduling/appointments/" + appointment.get("id").asLong() + "/payment-result"),
                "{\"paymentReference\":\"%s\",\"approved\":%s,\"amount\":120.00,\"currency\":\"PEN\"}"
                        .formatted(appointment.get("paymentReference").asString(), approved)));
    }

    @Test void identityIsRequiredAndOnlyParticipantsTakePart() throws Exception {
        mvc.perform(get("/api/v1/appointments")).andExpect(status().isUnauthorized());
        as(get("/api/v1/appointments"), 900, "ADMIN").andExpect(status().isForbidden());
        as(get("/api/v1/schedule/agenda"), 55, "PSYCHOLOGIST").andExpect(status().isForbidden());
        long clinician = publishedClinician();
        long request = contact(clinician, PATIENT);
        assertThat(contact(clinician, PATIENT)).as("an open chat is reused").isEqualTo(request);
        as(get("/api/v1/contact-requests/" + request + "/messages"), OTHER_PATIENT, "PATIENT").andExpect(status().isForbidden());
        as(get("/api/v1/contact-requests/" + request + "/messages"), PSYCHOLOGIST, "PSYCHOLOGIST")
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        as(body(post("/api/v1/contact-requests"), "{\"clinicianId\":999,\"message\":\"Hola\"}"), PATIENT, "PATIENT")
                .andExpect(status().is(422));
        as(body(patch("/api/v1/contact-requests/" + request + "/decision"), "{\"accept\":false}"), PATIENT, "PATIENT")
                .andExpect(status().isForbidden());
    }

    @Test void fullFlowFromContactToCompletedSessionEnablesTheReview() throws Exception {
        long clinician = publishedClinician();
        long request = contact(clinician, PATIENT);
        as(get("/api/v1/contact-requests"), PSYCHOLOGIST, "PSYCHOLOGIST").andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].lastMessage.body").exists());
        as(body(patch("/api/v1/contact-requests/" + request + "/decision"), "{\"accept\":true}"), PSYCHOLOGIST, "PSYCHOLOGIST")
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
        as(body(post("/api/v1/contact-requests/" + request + "/messages"), "{\"body\":\"Te propongo el jueves a las 15:00\"}"),
                PSYCHOLOGIST, "PSYCHOLOGIST").andExpect(status().isCreated());

        propose(request, at(8)).andExpect(status().is(422));
        long id = read(propose(request, at(15)).andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED")).andExpect(jsonPath("$.amount").value(120.00))).get("id").asLong();
        as(post("/api/v1/schedule/proposals/" + id + "/acceptance"), OTHER_PATIENT, "PATIENT").andExpect(status().isForbidden());
        var appointment = read(as(post("/api/v1/schedule/proposals/" + id + "/acceptance"), PATIENT, "PATIENT")
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("HELD"))
                .andExpect(jsonPath("$.holdRemainingSeconds").value(greaterThan(3500))));
        assertThat(events.stream(AppointmentChargeRequestedDto.class)).singleElement()
                .satisfies(charge -> assertThat(charge.idempotencyKey()).isEqualTo(appointment.get("paymentReference").asString()));

        propose(contact(clinician, OTHER_PATIENT), at(15)).andExpect(status().isConflict());
        as(get("/api/v1/schedule/clinicians/" + clinician + "/slots?date=" + day), PSYCHOLOGIST, "PSYCHOLOGIST")
                .andExpect(jsonPath("$[6].availability").value("HELD"));
        as(get("/api/v1/schedule/clinicians/" + clinician + "/slots?date=" + day), OTHER_PATIENT, "PATIENT")
                .andExpect(jsonPath("$[6].availability").value("UNAVAILABLE")).andExpect(jsonPath("$[5].availability").value("FREE"));

        pay(appointment, false).andExpect(jsonPath("$.message").value("HELD"));
        pay(appointment, true).andExpect(jsonPath("$.message").value("CONFIRMED"));
        pay(appointment, true).andExpect(jsonPath("$.message").value("CONFIRMED"));
        as(get("/api/v1/appointments"), PATIENT, "PATIENT").andExpect(jsonPath("$[0].status").value("CONFIRMED"));

        as(post("/api/v1/appointments/" + id + "/session/access"), PATIENT, "PATIENT").andExpect(status().is(422));
        var start = Instant.now().minus(Duration.ofMinutes(5));
        jdbc.update("update care_scheduling.appointments set starts_at = ?, ends_at = ? where id = ?",
                Timestamp.from(start), Timestamp.from(start.plus(Duration.ofMinutes(50))), id);
        as(post("/api/v1/appointments/" + id + "/session/access"), OTHER_PATIENT, "PATIENT").andExpect(status().isForbidden());
        as(post("/api/v1/appointments/" + id + "/session/access"), PATIENT, "PATIENT").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS")).andExpect(jsonPath("$.joinUrl").exists());
        as(get("/api/v1/appointments/" + id + "/session"), PSYCHOLOGIST, "PSYCHOLOGIST").andExpect(jsonPath("$.joinUrl").doesNotExist());

        as(get("/api/v1/appointments/" + id + "/authorized-summary"), PSYCHOLOGIST, "PSYCHOLOGIST").andExpect(status().isForbidden());
        as(body(post("/api/v1/appointments/" + id + "/session/closure"), "{\"outcome\":\"COMPLETED\"}"), PATIENT, "PATIENT")
                .andExpect(status().isForbidden());
        as(body(post("/api/v1/appointments/" + id + "/session/closure"), "{\"outcome\":\"COMPLETED\"}"), PSYCHOLOGIST, "PSYCHOLOGIST")
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"));

        as(body(post("/api/v1/clinicians/" + clinician + "/reviews"), """
            {"appointmentId":%d,"rating":5,"text":"Me ayudo mucho"}
            """.formatted(id)), PATIENT, "PATIENT").andExpect(status().isCreated());
    }

    @Test void expiredHoldReleasesTheSlotAndLatePaymentIsSentBackForRefund() throws Exception {
        long clinician = publishedClinician();
        var appointment = held(clinician, PATIENT, 16);
        long id = appointment.get("id").asLong();
        jdbc.update("update care_scheduling.slot_holds set expires_at = ? where appointment_id = ?",
                Timestamp.from(Instant.now().minusSeconds(1)), id);

        assertThat(commands.handle(new ExpireSlotHoldsCommand(Instant.now()))).isEqualTo(1);
        as(get("/api/v1/schedule/clinicians/" + clinician + "/slots?date=" + day), PSYCHOLOGIST, "PSYCHOLOGIST")
                .andExpect(jsonPath("$[7].availability").value("FREE"));

        var outcome = facade.applyPaymentResult(new AppointmentPaymentResultDto(id,
                appointment.get("paymentReference").asString(), new BigDecimal("120.00"), "PEN", true));
        assertThat(outcome.getOrElse(null)).isEqualTo("EXPIRED");
        assertThat(events.stream(AppointmentRefundRequestedDto.class)).singleElement()
                .satisfies(refund -> assertThat(refund.reason()).isEqualTo("LATE_PAYMENT"));
        held(clinician, OTHER_PATIENT, 16);
    }

    @Test void cancellingReleasesTheSlotAndOnlyPaidAppointmentsRequestRefunds() throws Exception {
        long clinician = publishedClinician();
        long heldId = held(clinician, PATIENT, 10).get("id").asLong();
        as(post("/api/v1/appointments/" + heldId + "/cancellation"), PATIENT, "PATIENT").andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        as(post("/api/v1/appointments/" + heldId + "/cancellation"), PATIENT, "PATIENT").andExpect(status().isConflict());
        assertThat(events.stream(AppointmentRefundRequestedDto.class)).isEmpty();

        var paid = held(clinician, OTHER_PATIENT, 10);
        pay(paid, true).andExpect(jsonPath("$.message").value("CONFIRMED"));
        as(post("/api/v1/appointments/" + paid.get("id").asLong() + "/cancellation"), PSYCHOLOGIST, "PSYCHOLOGIST")
                .andExpect(status().isOk());
        assertThat(events.stream(AppointmentRefundRequestedDto.class)).singleElement()
                .satisfies(refund -> assertThat(refund.reason()).isEqualTo("CANCELLED_BY_CLINICIAN"));
    }
}
