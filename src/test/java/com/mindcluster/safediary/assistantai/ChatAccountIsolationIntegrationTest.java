package com.mindcluster.safediary.assistantai;

import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.AssistantLanguageModel;
import com.mindcluster.safediary.assistantai.application.internal.outboundservices.llm.LlmReply;
import com.mindcluster.safediary.assistantai.infrastructure.persistence.jpa.repositories.ConversationSessionPersistenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:chat_isolation;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "iam.jwt.secret=chat-isolation-test-secret-at-least-32-characters",
    "spring.jpa.show-sql=false"
})
@AutoConfigureMockMvc
class ChatAccountIsolationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ConversationSessionPersistenceRepository sessions;
    @MockitoBean AssistantLanguageModel model;
    private final JsonMapper json = JsonMapper.builder().build();

    @BeforeEach void fakeModel() {
        when(model.generateReflection(any())).thenReturn(new LlmReply("Respuesta de prueba", "PENSIVENESS", 0.0, List.of()));
    }
    private JsonNode read(ResultActions result) throws Exception {
        return json.readTree(result.andReturn().getResponse().getContentAsString());
    }
    private String account() throws Exception {
        return read(mvc.perform(post("/api/v1/authentication/sign-up").contentType("application/json")
            .content("{\"email\":\"qa-" + UUID.randomUUID() + "@example.com\",\"password\":\"TestPassword123!\",\"role\":\"PATIENT\"}"))
            .andExpect(status().isCreated())).get("accessToken").asText();
    }
    private ResultActions as(String token, MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + token));
    }
    private String chat(String token, String conversationId) throws Exception {
        return read(as(token, post("/api/v1/assistant/chat").contentType("application/json").content(
            "{\"prompt\":\"Mensaje privado de prueba\",\"locale\":\"es\",\"conversationId\":" +
            (conversationId == null ? "null" : "\"" + conversationId + "\"") + "}"))
            .andExpect(status().isOk())).get("conversationId").asText();
    }

    @Test void twoAccountsCannotReadEditDeleteOrResumeEachOthersChats() throws Exception {
        var a = account();
        var b = account();
        var id = chat(a, null);
        var aChat = read(as(a, get("/api/v1/assistant/conversations/" + id)).andExpect(status().isOk()));
        var messageId = aChat.get("messages").get(0).get("id").asLong();
        assertThat(read(as(b, get("/api/v1/assistant/conversations")).andExpect(status().isOk())).size()).isZero();
        as(b, get("/api/v1/assistant/conversations/" + id)).andExpect(status().isNotFound());
        as(b, patch("/api/v1/assistant/conversations/" + id).contentType("application/json").content("{\"title\":\"Ajena\"}")).andExpect(status().isNotFound());
        as(b, delete("/api/v1/assistant/conversations/" + id)).andExpect(status().isNotFound());
        as(b, put("/api/v1/assistant/conversations/" + id + "/messages/" + messageId).contentType("application/json").content("{\"prompt\":\"Cambio ajeno\"}")).andExpect(status().isNotFound());
        as(b, post("/api/v1/assistant/conversations/" + id + "/regenerate").contentType("application/json").content("{}")).andExpect(status().isNotFound());
        as(b, get("/api/v1/conversation-sessions/" + id)).andExpect(status().isForbidden());
        as(b, post("/api/v1/conversation-sessions/" + id + "/messages").contentType("application/json").content("{\"content\":\"Ajeno\",\"locale\":\"es\"}")).andExpect(status().isForbidden());
        as(b, patch("/api/v1/conversation-sessions/" + id + "/close")).andExpect(status().isForbidden());
        var bId = chat(b, id);
        assertThat(bId).isNotEqualTo(id);
        assertThat(read(as(a, get("/api/v1/assistant/conversations/" + id)).andExpect(status().isOk())).get("messages").size()).isEqualTo(2);
        assertThat(read(as(b, get("/api/v1/assistant/conversations")).andExpect(status().isOk())).size()).isEqualTo(1);
        assertThat(read(as(a, get("/api/v1/assistant/conversations")).andExpect(status().isOk())).size()).isEqualTo(1);
    }

    @Test void legacySharedHistoryIsPreservedButCannotBeOpenedOrReused() throws Exception {
        var token = account();
        var id = chat(token, null);
        var legacy = sessions.findById(Long.valueOf(id)).orElseThrow();
        legacy.setOwnerVerified(false);
        sessions.saveAndFlush(legacy);
        assertThat(read(as(token, get("/api/v1/assistant/conversations")).andExpect(status().isOk())).size()).isZero();
        as(token, get("/api/v1/assistant/conversations/" + id)).andExpect(status().isNotFound());
        assertThat(chat(token, id)).isNotEqualTo(id);
        assertThat(sessions.findById(Long.valueOf(id))).isPresent();
        assertThat(sessions.findById(Long.valueOf(id)).orElseThrow().isOwnerVerified()).isFalse();
    }

    @Test void anonymousRequestsAndAccountIdSpoofingCannotAccessChatHistory() throws Exception {
        mvc.perform(get("/api/v1/assistant/conversations")).andExpect(status().isUnauthorized());
        var a = account();
        var b = account();
        var id = chat(a, null);
        var owner = sessions.findById(Long.valueOf(id)).orElseThrow().getAccountId();
        as(b, get("/api/v1/conversation-sessions").param("accountId", owner.toString())).andExpect(status().isForbidden());
        as(b, post("/api/v1/conversation-sessions").contentType("application/json")
            .content("{\"accountId\":" + owner + "}")).andExpect(status().isForbidden());
    }
}
