package com.dark.aiagent.interfaces.noticeboard.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.dark.aiagent.application.noticeboard.service.NoticeBoardService;
import com.dark.aiagent.domain.noticeboard.entity.Announcement;
import com.dark.aiagent.domain.noticeboard.entity.NoticeBoardItem;
import com.dark.aiagent.interfaces.noticeboard.dto.NoticeBoardDto.AnnouncementRequest;
import com.dark.aiagent.interfaces.noticeboard.dto.NoticeBoardDto.NoticeBoardItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(NoticeBoardController.class)
@AutoConfigureMockMvc(addFilters = false) // Ignore security filters for unit test
@ActiveProfiles("test")
class NoticeBoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoticeBoardService noticeBoardService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /rest/biz/v1/announcements -> returns list of announcements")
    void shouldReturnAnnouncements() throws Exception {
        Announcement announcement = Announcement.builder()
                .id(1L)
                .title("Test Title")
                .content("Test Content")
                .build();

        when(noticeBoardService.getAnnouncements(null)).thenReturn(List.of(announcement));

        mockMvc.perform(get("/rest/biz/v1/announcements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("Test Title"))
                .andExpect(jsonPath("$[0].content").value("Test Content"));
    }

    @Test
    @DisplayName("POST /rest/biz/v1/announcements -> creates a new announcement")
    void shouldCreateAnnouncement() throws Exception {
        AnnouncementRequest request = new AnnouncementRequest();
        request.setTitle("New Title");
        request.setContent("New Content");

        Announcement saved = Announcement.builder()
                .id(2L)
                .title("New Title")
                .content("New Content")
                .build();

        when(noticeBoardService.addAnnouncement(any(Announcement.class))).thenReturn(saved);

        mockMvc.perform(post("/rest/biz/v1/announcements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("New Title"));

        verify(noticeBoardService, times(1)).addAnnouncement(any(Announcement.class));
    }

    @Test
    @DisplayName("GET /rest/biz/v1/notice-board-items -> returns list of valid items")
    void shouldReturnNoticeBoardItems() throws Exception {
        NoticeBoardItem item = NoticeBoardItem.builder()
                .id(10L)
                .targetClient("Test Client")
                .contentUrl("http://example.com/test")
                .build();

        when(noticeBoardService.getValidNoticeBoardItems()).thenReturn(List.of(item));

        mockMvc.perform(get("/rest/biz/v1/notice-board-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].targetClient").value("Test Client"))
                .andExpect(jsonPath("$[0].contentUrl").value("http://example.com/test"));
    }

    @Test
    @DisplayName("POST /rest/biz/v1/notice-board-items -> creates a new item")
    void shouldCreateNoticeBoardItem() throws Exception {
        NoticeBoardItemRequest request = new NoticeBoardItemRequest();
        request.setTargetClient("New Client");
        request.setContentUrl("http://example.com/new");
        request.setExpireTime(OffsetDateTime.now().plusDays(1));

        NoticeBoardItem saved = NoticeBoardItem.builder()
                .id(11L)
                .targetClient("New Client")
                .contentUrl("http://example.com/new")
                .build();

        when(noticeBoardService.addNoticeBoardItem(any(NoticeBoardItem.class))).thenReturn(saved);

        mockMvc.perform(post("/rest/biz/v1/notice-board-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.targetClient").value("New Client"));

        verify(noticeBoardService, times(1)).addNoticeBoardItem(any(NoticeBoardItem.class));
    }

    @Test
    @DisplayName("POST /rest/biz/v1/notice-board-items/{id}/track-view -> calls track service")
    void shouldTrackItemView() throws Exception {
        Long itemId = 99L;

        mockMvc.perform(post("/rest/biz/v1/notice-board-items/" + itemId + "/track-view"))
                .andExpect(status().isOk());

        verify(noticeBoardService, times(1)).trackItemView(itemId);
    }

    @Test
    @DisplayName("PUT /rest/biz/v1/announcements/{id} -> updates announcement")
    void shouldUpdateAnnouncement() throws Exception {
        AnnouncementRequest request = new AnnouncementRequest();
        request.setTitle("Updated Title");
        request.setContent("Updated Content");
        request.setStatus(com.dark.aiagent.domain.noticeboard.enums.AnnouncementStatus.PUBLISHED);

        Announcement updated = Announcement.builder()
                .id(1L)
                .title("Updated Title")
                .content("Updated Content")
                .status(com.dark.aiagent.domain.noticeboard.enums.AnnouncementStatus.PUBLISHED)
                .build();

        when(noticeBoardService.updateAnnouncement(any(Long.class), any(Announcement.class))).thenReturn(updated);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/rest/biz/v1/announcements/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(noticeBoardService, times(1)).updateAnnouncement(any(Long.class), any(Announcement.class));
    }
}
