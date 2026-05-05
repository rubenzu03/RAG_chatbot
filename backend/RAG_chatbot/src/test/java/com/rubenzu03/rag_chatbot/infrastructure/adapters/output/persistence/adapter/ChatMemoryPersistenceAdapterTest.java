package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.adapter;

import com.rubenzu03.rag_chatbot.domain.dto.ChatResponse;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.ChatMemoryRepository;
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
    private ChatMemoryRepository chatMemoryRepository;

    @BeforeEach
    void setUp() {
        adapter = new ChatMemoryPersistenceAdapter(chatMemoryRepository);
    }

    @Test
    void testDeleteAllHistory() {
        adapter.deleteAllHistory();
        verify(chatMemoryRepository).deleteAll();
    }

    @Test
    void testDeleteHistoryById() {
        String userId = "user123";
        adapter.deleteHistoryById(userId);
        verify(chatMemoryRepository).deleteByUserId(userId);
    }

    @Test
    void testGetHistoryWithValidUserId() {
        String userId = "user456";
        List<ChatResponse> expectedHistory = List.of(
                new ChatResponse("Hi there", userId),
                new ChatResponse("How are you?", userId)
        );
        when(chatMemoryRepository.getChatHistoryByUserId(userId)).thenReturn(expectedHistory);
        List<ChatResponse> result = adapter.getHistory(userId);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyElementsOf(expectedHistory);
    }

    @Test
    void testGetHistoryReturnsEmptyList() {
        String userId = "unknown-user";
        when(chatMemoryRepository.getChatHistoryByUserId(userId)).thenReturn(List.of());

        List<ChatResponse> result = adapter.getHistory(userId);
        assertThat(result).isEmpty();
    }

    @Test
    void testGetHistoryCallsRepository() {
        String userId = "user789";
        when(chatMemoryRepository.getChatHistoryByUserId(anyString())).thenReturn(List.of());

        adapter.getHistory(userId);
        verify(chatMemoryRepository).getChatHistoryByUserId(userId);
    }

    @Test
    void testDeleteHistoryByIdWithDifferentUserIds() {
        String userId1 = "user1";
        String userId2 = "user2";

        adapter.deleteHistoryById(userId1);
        adapter.deleteHistoryById(userId2);

        verify(chatMemoryRepository).deleteByUserId(userId1);
        verify(chatMemoryRepository).deleteByUserId(userId2);
    }

    @Test
    void testGetHistoryWithMultipleMessages() {
        String userId = "user-multi";
        List<ChatResponse> multipleMessages = List.of(
                new ChatResponse("Message 1", userId),
                new ChatResponse("Message 2", userId),
                new ChatResponse("Message 3", userId),
                new ChatResponse("Message 4", userId)
        );
        when(chatMemoryRepository.getChatHistoryByUserId(userId)).thenReturn(multipleMessages);
        List<ChatResponse> result = adapter.getHistory(userId);
        assertThat(result).hasSize(4);
    }
}
