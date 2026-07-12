package com.dark.aiagent.module.aidev.interfaces.rest;

import com.dark.aiagent.module.aidev.application.AiDevAgentProfileUseCase;
import com.dark.aiagent.module.aidev.domain.entity.AiDevAgentProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AiDevAgentProfileController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class AiDevAgentProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiDevAgentProfileUseCase useCase;

    @Test
    void shouldPassLocalSyncPathToUseCaseWhenUpdatingProfile() throws Exception {
        AiDevAgentProfile mockProfile = new AiDevAgentProfile(
                "1", "FSA", "http://fsa.api", "token", "gemini-1.5", "avatar", "prompt", "D:/fsa/sync", "Hermes Agent",
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(useCase.updateProfile(
                eq("FSA"),
                eq("http://fsa.api"),
                eq("token"),
                eq("gemini-1.5"),
                eq("avatar"),
                eq("prompt"),
                eq("D:/fsa/sync"),
                eq("Hermes Agent")
        )).thenReturn(mockProfile);

        String jsonPayload = """
                {
                  "roleName": "FSA",
                  "baseUrl": "http://fsa.api",
                  "apiToken": "token",
                  "modelName": "gemini-1.5",
                  "avatar": "avatar",
                  "systemPrompt": "prompt",
                  "localSyncPath": "D:/fsa/sync",
                  "agentType": "Hermes Agent"
                }
                """;

        mockMvc.perform(put("/rest/biz/v1/ai-dev/profiles/FSA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk());

        verify(useCase, times(1)).updateProfile(
                "FSA", "http://fsa.api", "token", "gemini-1.5", "avatar", "prompt", "D:/fsa/sync", "Hermes Agent"
        );
    }
}
