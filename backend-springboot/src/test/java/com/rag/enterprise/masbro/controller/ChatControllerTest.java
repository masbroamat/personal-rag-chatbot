package com.rag.enterprise.masbro.controller;

import com.rag.enterprise.masbro.service.AgentService;
import com.rag.enterprise.masbro.service.HistoryService;
import com.rag.enterprise.masbro.service.IngestionService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgentService agentService;

    @MockitoBean
    private IngestionService ingestionService;

    @MockitoBean
    private ChatLanguageModel chatLanguageModel;

    @MockitoBean
    private HistoryService historyService;

    @Test
    void testChatEndpoint_ShouldReturnResponse() throws Exception{
        String userQuestion = "Hello Masbro";
        String aiResponse = "Hello! How can I help you check the server logs?";

        given(agentService.chat(anyString(), anyString())).willReturn(aiResponse);

        mockMvc.perform(get("/api/rag/v1/chat")
                .param("question", userQuestion)
                .param("chatId", "12345")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string(aiResponse));

        verify(agentService, times(1)).chat(any(), any());
    }
}
