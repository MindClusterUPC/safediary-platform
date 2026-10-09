package com.mindcluster.safediary.cliniciandirectory;

import com.mindcluster.safediary.cliniciandirectory.interfaces.acl.ClinicianDirectoryContextFacade;
import com.mindcluster.safediary.cliniciandirectory.interfaces.acl.dto.CompletedCareSessionDto;
import com.mindcluster.safediary.cliniciandirectory.application.commandservices.DirectoryCommandService;
import com.mindcluster.safediary.cliniciandirectory.domain.model.commands.*;
import com.mindcluster.safediary.cliniciandirectory.domain.model.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={
        "spring.profiles.active=dev",
        "spring.datasource.url=jdbc:h2:mem:directory_api;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "clinician-directory.demo-identity.enabled=true",
        "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class DirectoryApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired ClinicianDirectoryContextFacade facade;
    @Autowired DirectoryCommandService commands;
    private final JsonMapper json = JsonMapper.builder().build();
    private static final String PROFILE = """
        {"displayName":"Ana Torres","professionalTitle":"Clinical psychologist","bio":"Care for anxiety and stress",
         "bannerRef":"https://example.org/banner.png","specialties":["Anxiety","Stress"],
         "amount":80.00,"currency":"PEN","durationMinutes":50}
        """;

    @BeforeEach void cleanDirectory() {
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
    private long id(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString()).get("id").asLong();
    }
    private long draft(long account) throws Exception {
        return id(as(body(post("/api/v1/clinicians"), PROFILE), account, "PSYCHOLOGIST").andExpect(status().isCreated()));
    }
    private long request(long clinician, long account) throws Exception {
        return id(as(body(post("/api/v1/clinicians/" + clinician + "/verification-requests"), """
            {"licenseNumber":"CPP-1234","specialty":"Anxiety","documentRef":"private://credentials/license.pdf"}
            """), account, "PSYCHOLOGIST").andExpect(status().isCreated()));
    }
    private long published(long account) throws Exception {
        long clinician = draft(account);
        long verification = request(clinician, account);
        as(body(patch("/api/v1/verification-requests/" + verification + "/decision"), """
            {"decision":"APPROVED"}
            """), 900, "ADMIN").andExpect(status().isOk());
        as(post("/api/v1/clinicians/" + clinician + "/publication"), account, "PSYCHOLOGIST").andExpect(status().isOk());
        return clinician;
    }
    private void completed(long clinician, long patient, long appointment) {
        var event = new CompletedCareSessionDto(appointment, clinician, patient, 50, Instant.parse("2026-01-01T12:00:00Z"));
        assertThat(facade.recordCompletedSession(event).isSuccess()).isTrue();
    }
    private long review(long clinician, long patient, long appointment, int rating) throws Exception {
        completed(clinician, patient, appointment);
        return id(as(body(post("/api/v1/clinicians/" + clinician + "/reviews"), """
            {"appointmentId":%d,"rating":%d,"text":"Helpful session"}
            """.formatted(appointment, rating)), patient, "PATIENT").andExpect(status().isCreated()));
    }

    @Test void identityOwnershipAndDraftVisibility() throws Exception {
        mvc.perform(body(post("/api/v1/clinicians"), PROFILE)).andExpect(status().isUnauthorized());
        as(body(post("/api/v1/clinicians"), PROFILE), 1, "PATIENT").andExpect(status().isForbidden());
        long clinician = draft(1);
        as(body(post("/api/v1/clinicians"), PROFILE), 1, "PSYCHOLOGIST").andExpect(status().isConflict());
        as(get("/api/v1/clinicians/me"), 1, "PSYCHOLOGIST").andExpect(status().isOk())
                .andExpect(jsonPath("$.publicationStatus").value("DRAFT"));
        mvc.perform(get("/api/v1/clinicians")).andExpect(content().json("[]"));
        mvc.perform(get("/api/v1/clinicians/" + clinician)).andExpect(status().isNotFound());
        as(post("/api/v1/clinicians/" + clinician + "/publication"), 1, "PSYCHOLOGIST").andExpect(status().isBadRequest());
        as(body(put("/api/v1/clinicians/" + clinician), PROFILE), 2, "PSYCHOLOGIST").andExpect(status().isForbidden());
        as(get("/api/v1/clinicians/" + clinician + "/verification"), 2, "PSYCHOLOGIST").andExpect(status().isForbidden());
    }

    @Test void verificationCanBeRejectedCorrectedAndApprovedWithoutExposingPrivateDocuments() throws Exception {
        long clinician = draft(1);
        long verification = request(clinician, 1);
        as(get("/api/v1/verification-requests"), 2, "PATIENT").andExpect(status().isForbidden());
        as(get("/api/v1/verification-requests"), 900, "ADMIN").andExpect(jsonPath("$[0].documentRef").value("private://credentials/license.pdf"));
        as(body(patch("/api/v1/verification-requests/" + verification + "/decision"), """
            {"decision":"REJECTED"}
            """), 900, "ADMIN").andExpect(status().isBadRequest());
        as(body(patch("/api/v1/verification-requests/" + verification + "/decision"), """
            {"decision":"REJECTED","reason":"Unreadable license"}
            """), 900, "ADMIN").andExpect(status().isOk()).andExpect(jsonPath("$.reviewedByAccountId").value(900));
        as(post("/api/v1/clinicians/" + clinician + "/publication"), 1, "PSYCHOLOGIST").andExpect(status().isBadRequest());
        long corrected = request(clinician, 1);
        as(body(patch("/api/v1/verification-requests/" + corrected + "/decision"), """
            {"decision":"APPROVED"}
            """), 1, "PSYCHOLOGIST").andExpect(status().isForbidden());
        as(body(patch("/api/v1/verification-requests/" + corrected + "/decision"), """
            {"decision":"APPROVED"}
            """), 900, "ADMIN").andExpect(status().isOk());
        as(post("/api/v1/clinicians/" + clinician + "/publication"), 1, "PSYCHOLOGIST").andExpect(status().isOk());
        mvc.perform(get("/api/v1/clinicians/" + clinician)).andExpect(status().isOk())
                .andExpect(jsonPath("$.documentRef").doesNotExist()).andExpect(jsonPath("$.accountId").doesNotExist());
        as(delete("/api/v1/clinicians/" + clinician + "/publication"), 1, "PSYCHOLOGIST").andExpect(status().isOk());
        mvc.perform(get("/api/v1/clinicians/" + clinician)).andExpect(status().isNotFound());
    }

    @Test void verificationResubmissionHidesPublicProfileAndPreventsDuplicatePendingRequests() throws Exception {
        long clinician = published(1);
        request(clinician, 1);
        mvc.perform(get("/api/v1/clinicians")).andExpect(content().json("[]"));
        as(body(post("/api/v1/clinicians/" + clinician + "/verification-requests"), """
            {"licenseNumber":"CPP-1234","specialty":"Anxiety","documentRef":"private://credentials/license.pdf"}
            """), 1, "PSYCHOLOGIST").andExpect(status().isConflict());
    }

    @Test void ratesAreVersionedAndPreviouslyReadBookingQuotesStayUnchanged() throws Exception {
        long clinician = published(1);
        var quote = facade.fetchPublishedClinicianRate(clinician).orElseThrow();
        as(body(put("/api/v1/clinicians/" + clinician), PROFILE.replace("80.00", "95.00")), 1, "PSYCHOLOGIST")
                .andExpect(status().isOk()).andExpect(jsonPath("$.rate.version").value(2));
        assertThat(quote.amount()).isEqualByComparingTo("80.00");
        assertThat(facade.fetchPublishedClinicianRate(clinician).orElseThrow().amount()).isEqualByComparingTo("95.00");
        assertThat(jdbc.queryForObject("select count(*) from clinician_directory.consultation_rates where clinician_profile_id=?", Integer.class, clinician)).isEqualTo(2);
        as(body(put("/api/v1/clinicians/" + clinician), PROFILE.replace("80.00", "95.00")), 1, "PSYCHOLOGIST")
                .andExpect(jsonPath("$.rate.version").value(2));
    }

    @Test void searchFiltersPaginationAndInputValidation() throws Exception {
        published(1);
        mvc.perform(get("/api/v1/clinicians").param("text", "ANA").param("specialty", "anxiety")
                .param("maxAmount", "80").param("currency", "PEN")).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/v1/clinicians").param("maxAmount", "79").param("currency", "PEN")).andExpect(content().json("[]"));
        mvc.perform(get("/api/v1/clinicians").param("currency", "USD")).andExpect(content().json("[]"));
        mvc.perform(get("/api/v1/clinicians").param("page", "1")).andExpect(content().json("[]"));
        mvc.perform(get("/api/v1/clinicians").param("size", "0")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/clinicians").param("maxAmount", "80")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/clinicians").param("page", "bad")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/clinicians/not-an-id")).andExpect(status().isBadRequest());
        as(body(post("/api/v1/clinicians"), PROFILE.replace("https://example.org/banner.png", "private://credentials/license.pdf")),
                2, "PSYCHOLOGIST").andExpect(status().isBadRequest());
        as(body(post("/api/v1/clinicians"), PROFILE.replace("80.00", "-1")), 2, "PSYCHOLOGIST").andExpect(status().isBadRequest());
    }

    @Test void onlyCompletedSessionsAllowOneReviewAndEventReplaysAreIdempotent() throws Exception {
        long clinician = published(1);
        String payload = "{\"appointmentId\":10,\"rating\":5}";
        as(body(post("/api/v1/clinicians/" + clinician + "/reviews"), payload), 2, "PATIENT").andExpect(status().isConflict());
        var event = new CompletedCareSessionDto(10L, clinician, 2L, 50, Instant.parse("2026-01-01T12:00:00Z"));
        assertThat(facade.recordCompletedSession(event).isSuccess()).isTrue();
        assertThat(facade.recordCompletedSession(event).isSuccess()).isTrue();
        assertThat(facade.recordCompletedSession(new CompletedCareSessionDto(10L, clinician, 3L, 50, event.completedAt())).isFailure()).isTrue();
        as(body(post("/api/v1/clinicians/" + clinician + "/reviews"), payload), 3, "PATIENT").andExpect(status().isForbidden());
        as(body(post("/api/v1/clinicians/" + clinician + "/reviews"), payload), 2, "PATIENT").andExpect(status().isCreated());
        as(body(post("/api/v1/clinicians/" + clinician + "/reviews"), payload), 2, "PATIENT").andExpect(status().isConflict());
        mvc.perform(get("/api/v1/clinicians/" + clinician + "/reviews")).andExpect(jsonPath("$[0].patientAccountId").doesNotExist())
                .andExpect(jsonPath("$[0].appointmentId").doesNotExist());
        mvc.perform(get("/api/v1/clinicians/" + clinician + "/rating")).andExpect(jsonPath("$.average").value(5))
                .andExpect(jsonPath("$.reviewCount").value(1));
        as(get("/api/v1/clinicians/" + clinician + "/trust-score"), 1, "PSYCHOLOGIST")
                .andExpect(jsonPath("$.completedSessions").value(1)).andExpect(jsonPath("$.sufficientData").value(false));
    }

    @Test void helpfulVotesAreIdempotentPrivateAndIndependentOfRatings() throws Exception {
        long clinician = published(1), review = review(clinician, 2, 10, 4);
        String url = "/api/v1/reviews/" + review + "/helpful-vote";
        as(body(put(url), "{\"helpful\":true}"), 2, "PATIENT").andExpect(status().isForbidden());
        as(body(put(url), "{\"helpful\":true}"), 3, "PATIENT").andExpect(jsonPath("$.helpfulCount").value(1));
        as(body(put(url), "{\"helpful\":true}"), 3, "PATIENT").andExpect(jsonPath("$.helpfulCount").value(1))
                .andExpect(jsonPath("$.accountId").doesNotExist());
        mvc.perform(get("/api/v1/clinicians/" + clinician + "/rating")).andExpect(jsonPath("$.average").value(4));
        as(body(put(url), "{\"helpful\":false}"), 3, "PATIENT").andExpect(jsonPath("$.helpfulCount").value(0));
    }

    @Test void authorDeletionRequiresConfirmationRecalculatesAndKeepsAppointmentUniqueness() throws Exception {
        long clinician = published(1), review = review(clinician, 2, 10, 5);
        as(body(put("/api/v1/reviews/" + review + "/helpful-vote"), "{\"helpful\":true}"), 3, "PATIENT").andExpect(status().isOk());
        as(delete("/api/v1/reviews/" + review).param("confirmed", "true"), 3, "PATIENT").andExpect(status().isForbidden());
        as(delete("/api/v1/reviews/" + review), 2, "PATIENT").andExpect(status().isBadRequest());
        as(delete("/api/v1/reviews/" + review).param("confirmed", "true"), 2, "PATIENT").andExpect(status().isOk());
        mvc.perform(get("/api/v1/clinicians/" + clinician + "/reviews")).andExpect(content().json("[]"));
        mvc.perform(get("/api/v1/clinicians/" + clinician + "/rating")).andExpect(jsonPath("$.reviewCount").value(0))
                .andExpect(jsonPath("$.average").isEmpty());
        assertThat(jdbc.queryForObject("select count(*) from clinician_directory.review_helpful_votes where active=true", Integer.class)).isZero();
        assertThat(jdbc.queryForObject("select review_text from clinician_directory.reviews where id=?", String.class, review)).isNull();
        as(body(post("/api/v1/clinicians/" + clinician + "/reviews"), "{\"appointmentId\":10,\"rating\":5}"), 2, "PATIENT")
                .andExpect(status().isConflict());
    }

    @Test void reportsDoNotHideReviewsUntilAnIndependentModeratorResolvesThem() throws Exception {
        long clinician = published(1), review = review(clinician, 2, 10, 2);
        String url = "/api/v1/reviews/" + review + "/reports";
        long report = id(as(body(post(url), "{\"reason\":\"SPAM\",\"comment\":\"Please inspect\"}"), 3, "PATIENT")
                .andExpect(status().isCreated()).andExpect(jsonPath("$.reporterAccountId").doesNotExist()));
        as(body(post(url), "{\"reason\":\"SPAM\"}"), 3, "PATIENT").andExpect(status().isConflict());
        mvc.perform(get("/api/v1/clinicians/" + clinician + "/reviews")).andExpect(jsonPath("$.length()").value(1));
        as(get("/api/v1/review-reports"), 2, "PATIENT").andExpect(status().isForbidden());
        as(get("/api/v1/review-reports"), 901, "MODERATOR").andExpect(jsonPath("$.length()").value(1));
        String resolution = "{\"removeReview\":true,\"resolution\":\"Confirmed spam\"}";
        as(body(patch("/api/v1/review-reports/" + report + "/resolution"), resolution), 3, "MODERATOR").andExpect(status().isForbidden());
        as(body(patch("/api/v1/review-reports/" + report + "/resolution"), resolution), 901, "MODERATOR")
                .andExpect(status().isOk()).andExpect(jsonPath("$.resolvedByAccountId").value(901));
        mvc.perform(get("/api/v1/clinicians/" + clinician + "/reviews")).andExpect(content().json("[]"));
        mvc.perform(get("/api/v1/clinicians/" + clinician + "/rating")).andExpect(jsonPath("$.reviewCount").value(0));
        as(body(patch("/api/v1/review-reports/" + report + "/resolution"), resolution), 901, "MODERATOR").andExpect(status().isConflict());
    }

    @Test void trustHasAnExplicitInsufficientDataStateAndNoVolumeBonus() throws Exception {
        long clinician = published(1);
        review(clinician, 2, 10, 5); review(clinician, 3, 11, 4); review(clinician, 4, 12, 3);
        as(get("/api/v1/clinicians/" + clinician + "/trust-score"), 1, "PSYCHOLOGIST")
                .andExpect(jsonPath("$.sufficientData").value(true)).andExpect(jsonPath("$.score").value(80));
        completed(clinician, 5, 13);
        as(get("/api/v1/clinicians/" + clinician + "/trust-score"), 1, "PSYCHOLOGIST")
                .andExpect(jsonPath("$.score").value(80)).andExpect(jsonPath("$.completedSessions").value(4));
        as(get("/api/v1/clinicians/" + clinician + "/trust-score"), 2, "PSYCHOLOGIST").andExpect(status().isForbidden());
    }

    @Test void swaggerContainsDirectoryEndpointsAndDevelopmentHeaders() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/clinicians'].post.parameters[0].name").value("X-Account-Id"))
                .andExpect(jsonPath("$.paths['/api/v1/review-reports/{id}/resolution'].patch").exists());
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
    }

    @Test void concurrentDuplicateReviewsHaveExactlyOneWinner() throws Exception {
        long clinician = published(1);
        completed(clinician, 2, 10);
        var command = new PublishReviewCommand(new DirectoryActor(2L, DirectoryRole.PATIENT), clinician, 10L, 5, "Helpful");
        var ready = new java.util.concurrent.CountDownLatch(2);
        var start = new java.util.concurrent.CountDownLatch(1);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            java.util.concurrent.Callable<Boolean> action = () -> {
                ready.countDown(); start.await(); return commands.handle(command).isSuccess();
            };
            var first = executor.submit(action); var second = executor.submit(action);
            assertThat(ready.await(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(java.util.List.of(first.get(10, java.util.concurrent.TimeUnit.SECONDS),
                    second.get(10, java.util.concurrent.TimeUnit.SECONDS))).containsExactlyInAnyOrder(true, false);
        }
        mvc.perform(get("/api/v1/clinicians/" + clinician + "/rating")).andExpect(jsonPath("$.reviewCount").value(1));
    }

    @Test void concurrentDeletionAndVoteCannotLeaveAnActiveVoteOnADeletedReview() throws Exception {
        long clinician = published(1), review = review(clinician, 2, 10, 5);
        var start = new java.util.concurrent.CountDownLatch(1);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var deletion = executor.submit(() -> {
                start.await(); return commands.handle(new DeleteOwnReviewCommand(new DirectoryActor(2L, DirectoryRole.PATIENT), review, true));
            });
            var vote = executor.submit(() -> {
                start.await(); return commands.handle(new ToggleReviewHelpfulVoteCommand(new DirectoryActor(3L, DirectoryRole.PATIENT), review, true));
            });
            start.countDown();
            assertThat(deletion.get(10, java.util.concurrent.TimeUnit.SECONDS).isSuccess()).isTrue();
            vote.get(10, java.util.concurrent.TimeUnit.SECONDS);
        }
        assertThat(jdbc.queryForObject("select count(*) from clinician_directory.review_helpful_votes where active=true", Integer.class)).isZero();
    }
}
