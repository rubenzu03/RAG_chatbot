package com.rubenzu03.rag_chatbot.infrastructure.adapters.input.rest;

import com.rubenzu03.rag_chatbot.application.ports.input.ChatUseCase;
import com.rubenzu03.rag_chatbot.domain.dto.ChatHistoryMessage;
import com.rubenzu03.rag_chatbot.domain.dto.ChatHistoryResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatControllerTest {

    private ChatUseCase chatUseCase;
    private ChatController controller;

    @BeforeEach
    void setUp() {
        chatUseCase = mock(ChatUseCase.class);
        controller = new ChatController(chatUseCase);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", "N/A")
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getChatHistory_buildsConversationKeyFromUserAndConversationId() {
        when(chatUseCase.getHistory("user@example.com::conv"))
                .thenReturn(List.of(new ChatHistoryMessage("user", "hi")));

        ChatHistoryResponse history = controller.getChatHistory("conv");

        assertThat(history.history()).hasSize(1);
        verify(chatUseCase).getHistory("user@example.com::conv");
    }

    @Test
    void deleteChatHistory_buildsDefaultConversationKeyWhenMissingConversationId() {
        controller.deleteChatHistory(null);

        verify(chatUseCase).deleteHistory("user@example.com::default");
    }
}

