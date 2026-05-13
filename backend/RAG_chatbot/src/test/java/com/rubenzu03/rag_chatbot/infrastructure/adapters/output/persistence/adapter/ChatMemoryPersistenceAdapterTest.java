package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.adapter;

import com.rubenzu03.rag_chatbot.domain.dto.ChatHistoryMessage;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMemoryPersistenceAdapterTest {

    private ChatMemoryPersistenceAdapter adapter;

    @Mock
    private JdbcChatMemoryRepository chatMemoryRepository;

    @BeforeEach
    void setUp() {
        adapter = new ChatMemoryPersistenceAdapter(chatMemoryRepository);
    }

    @Test
    void testDeleteAllHistory() {
        when(chatMemoryRepository.findConversationIds()).thenReturn(List.of("conv-1", "conv-2"));

        adapter.deleteAllHistory();

        verify(chatMemoryRepository).deleteByConversationId("conv-1");
        verify(chatMemoryRepository).deleteByConversationId("conv-2");
    }

    @Test
    void testDeleteHistoryById() {
        String userId = "user123";
        adapter.deleteHistoryById(userId);
        verify(chatMemoryRepository).deleteByConversationId(userId);
    }

    @Test
    void testGetHistoryWithValidUserId() {
        String userId = "user456";
        when(chatMemoryRepository.findByConversationId(userId)).thenReturn(List.of(
            new UserMessage("Hi there"),
            new AssistantMessage("How are you?")));

        List<ChatHistoryMessage> result = adapter.getHistory(userId);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(
            new ChatHistoryMessage("user", "Hi there"),
            new ChatHistoryMessage("assistant", "How are you?"));
    }

    @Test
    void testGetHistoryReturnsEmptyList() {
        String userId = "unknown-user";
        when(chatMemoryRepository.findByConversationId(userId)).thenReturn(List.of());

        List<ChatHistoryMessage> result = adapter.getHistory(userId);
        assertThat(result).isEmpty();
    }

    @Test
    void testGetHistoryCallsRepository() {
        String userId = "user789";
        when(chatMemoryRepository.findByConversationId(anyString())).thenReturn(List.of());

        adapter.getHistory(userId);
        verify(chatMemoryRepository).findByConversationId(userId);
    }

    @Test
    void testDeleteHistoryByIdWithDifferentUserIds() {
        String userId1 = "user1";
        String userId2 = "user2";

        adapter.deleteHistoryById(userId1);
        adapter.deleteHistoryById(userId2);

        verify(chatMemoryRepository).deleteByConversationId(userId1);
        verify(chatMemoryRepository).deleteByConversationId(userId2);
    }

    @Test
    void testGetHistoryWithMultipleMessages() {
        String userId = "user-multi";
        when(chatMemoryRepository.findByConversationId(userId)).thenReturn(List.of(
            new UserMessage("Message 1"),
            new AssistantMessage("Message 2"),
            new UserMessage("Message 3"),
            new AssistantMessage("Message 4")));

        List<ChatHistoryMessage> result = adapter.getHistory(userId);
        assertThat(result).hasSize(4);
    }
}
