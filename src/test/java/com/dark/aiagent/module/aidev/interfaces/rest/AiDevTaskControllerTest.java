package com.dark.aiagent.module.aidev.interfaces.rest;

import com.dark.aiagent.module.aidev.application.AiDevIntegrationUseCase;
import com.dark.aiagent.module.aidev.domain.entity.AiDevChatMessage;
import com.dark.aiagent.module.aidev.domain.entity.AiDevTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AiDevTaskController.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = false)
class AiDevTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiDevIntegrationUseCase aiDevTaskUseCase;

    @Test
    void shouldReturnTaskList() throws Exception {
        AiDevTask task = new AiDevTask(
                "task-123", "Setup DB", "Init sql", "PLANNING", "branch-1", 
                0.05, null, OffsetDateTime.now(), OffsetDateTime.now()
        );
        when(aiDevTaskUseCase.getAllTasks()).thenReturn(List.of(task));

        mockMvc.perform(get("/rest/biz/v1/ai-dev/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("task-123"))
                .andExpect(jsonPath("$[0].title").value("Setup DB"))
                .andExpect(jsonPath("$[0].status").value("PLANNING"));
    }

    @Test
    void shouldResumeTask() throws Exception {
        doNothing().when(aiDevTaskUseCase).resumeTask("task-123", null);

        mockMvc.perform(post("/rest/biz/v1/ai-dev/tasks/task-123/resume"))
                .andExpect(status().isOk());

        verify(aiDevTaskUseCase, times(1)).resumeTask("task-123", null);
    }

    @Test
    void shouldRollbackTask() throws Exception {
        doNothing().when(aiDevTaskUseCase).rollbackTask("task-123");

        mockMvc.perform(post("/rest/biz/v1/ai-dev/tasks/task-123/rollback"))
                .andExpect(status().isOk());

        verify(aiDevTaskUseCase, times(1)).rollbackTask("task-123");
    }

    @Test
    void shouldDeleteTask() throws Exception {
        doNothing().when(aiDevTaskUseCase).deleteTask("task-123");

        mockMvc.perform(delete("/rest/biz/v1/ai-dev/tasks/task-123"))
                .andExpect(status().isOk());

        verify(aiDevTaskUseCase, times(1)).deleteTask("task-123");
    }

    @Test
    void shouldReopenTask() throws Exception {
        doNothing().when(aiDevTaskUseCase).reopenTask("task-123");

        mockMvc.perform(post("/rest/biz/v1/ai-dev/tasks/task-123/reopen"))
                .andExpect(status().isOk());

        verify(aiDevTaskUseCase, times(1)).reopenTask("task-123");
    }

    @Test
    void shouldUpdateTaskAssignedRoles() throws Exception {
        List<String> roles = List.of("PLANNER", "GENERATOR");
        doNothing().when(aiDevTaskUseCase).updateTaskAssignedRoles("task-123", roles);

        mockMvc.perform(put("/rest/biz/v1/ai-dev/tasks/task-123/assigned-roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"PLANNER\",\"GENERATOR\"]"))
                .andExpect(status().isOk());

        verify(aiDevTaskUseCase, times(1)).updateTaskAssignedRoles("task-123", roles);
    }
}
