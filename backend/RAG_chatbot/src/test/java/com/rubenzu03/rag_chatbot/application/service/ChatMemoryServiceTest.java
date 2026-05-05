package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.application.ports.output.ChatMemoryPort;
import com.rubenzu03.rag_chatbot.domain.dto.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatMemoryServiceTest {

    private ChatMemoryPort chatMemoryPort;
    private ChatMemoryService service;

    @BeforeEach
    void setUp() {
        chatMemoryPort = mock(ChatMemoryPort.class);
        service = new ChatMemoryService(chatMemoryPort);
    }

    @Test
    void getHistory_delegates() {
        when(chatMemoryPort.getHistory("user")).thenReturn(List.of(new ChatResponse("hi", "user")));
        List<ChatResponse> history = service.getHistory("user");

        assertThat(history).hasSize(1);
    }

    @Test
    void deleteHistory_delegates() {
        service.deleteHistory("user");
        verify(chatMemoryPort).deleteHistoryById("user");
    }

    @Test
    void deleteAllHistory_delegates() {
        service.deleteAllHistory();
        verify(chatMemoryPort).deleteAllHistory();
    }
}
