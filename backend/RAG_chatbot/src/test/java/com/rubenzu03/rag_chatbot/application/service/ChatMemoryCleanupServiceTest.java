package com.rubenzu03.rag_chatbot.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMemoryCleanupServiceTest {

    private ChatMemoryCleanupService service;

    @Mock
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        service = new ChatMemoryCleanupService(jdbc);
    }

    @Test
    void testCleanupOldChatMemories_Success() {
        int expectedDeletedRows = 5;
        when(jdbc.update(anyString(), any(LocalDateTime.class))).thenReturn(expectedDeletedRows);

        int result = service.cleanupOldChatMemories();

        assertThat(result).isEqualTo(expectedDeletedRows);
        verify(jdbc).update(anyString(), any(LocalDateTime.class));
    }

    @Test
    void testCleanupOldChatMemories_NoRowsDeleted() {
        when(jdbc.update(anyString(), any(LocalDateTime.class))).thenReturn(0);

        int result = service.cleanupOldChatMemories();

        assertThat(result).isEqualTo(0);
        verify(jdbc).update(anyString(), any(LocalDateTime.class));
    }

    @Test
    void testCleanupOldChatMemories_WithCorrectSQL() {
        when(jdbc.update(anyString(), any(LocalDateTime.class))).thenReturn(3);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

        service.cleanupOldChatMemories();

        verify(jdbc).update(sqlCaptor.capture(), any(LocalDateTime.class));
        String executedSQL = sqlCaptor.getValue();
        assertThat(executedSQL).contains("DELETE FROM spring_ai_chat_memory");
        assertThat(executedSQL).contains("created_at");
    }

    @Test
    void testCleanupOldChatMemories_ThrowsException() {
        when(jdbc.update(anyString(), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database connection error"));

        assertThatThrownBy(() -> service.cleanupOldChatMemories())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to cleanup old chat memories");
    }

    @Test
    void testCleanupChatMemoriesByDays_Success() {
        int days = 7;
        int expectedDeletedRows = 10;
        when(jdbc.update(anyString(), any(LocalDateTime.class))).thenReturn(expectedDeletedRows);

        int result = service.cleanupChatMemoriesByDays(days);

        assertThat(result).isEqualTo(expectedDeletedRows);
        verify(jdbc).update(anyString(), any(LocalDateTime.class));
    }

    @Test
    void testCleanupChatMemoriesByDays_ZeroDays() {
        int days = 0;
        when(jdbc.update(anyString(), any(LocalDateTime.class))).thenReturn(0);

        int result = service.cleanupChatMemoriesByDays(days);

        assertThat(result).isEqualTo(0);
        verify(jdbc).update(anyString(), any(LocalDateTime.class));
    }

    @Test
    void testCleanupChatMemoriesByDays_LargeDaysValue() {
        int days = 90;
        int expectedDeletedRows = 2;
        when(jdbc.update(anyString(), any(LocalDateTime.class))).thenReturn(expectedDeletedRows);

        int result = service.cleanupChatMemoriesByDays(days);

        assertThat(result).isEqualTo(expectedDeletedRows);
    }

    @Test
    void testCleanupChatMemoriesByDays_ThrowsException() {
        when(jdbc.update(anyString(), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> service.cleanupChatMemoriesByDays(5))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to cleanup chat memories");
    }

    @Test
    void testCountOldChatMemories_Success() {
        long expectedCount = 15L;
        when(jdbc.queryForObject(anyString(), any(Class.class), any(LocalDateTime.class)))
                .thenReturn(expectedCount);

        long result = service.countOldChatMemories();
        assertThat(result).isEqualTo(expectedCount);
        verify(jdbc).queryForObject(anyString(), any(Class.class), any(LocalDateTime.class));
    }

    @Test
    void testCountOldChatMemories_ZeroEntries() {
        when(jdbc.queryForObject(anyString(), any(Class.class), any(LocalDateTime.class)))
                .thenReturn(0L);

        long result = service.countOldChatMemories();
        assertThat(result).isEqualTo(0L);
    }

    @Test
    void testCountOldChatMemories_NullResult() {
        when(jdbc.queryForObject(anyString(), any(Class.class), any(LocalDateTime.class)))
                .thenReturn(null);

        long result = service.countOldChatMemories();
        assertThat(result).isEqualTo(0L);
    }

    @Test
    void testCountOldChatMemories_ThrowsException() {
        when(jdbc.queryForObject(anyString(), any(Class.class), any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> service.countOldChatMemories())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to count old chat memories");
    }

    @Test
    void testCleanupOldChatMemories_DeletesEntriesOlderThan3Days() {
        when(jdbc.update(anyString(), any(LocalDateTime.class))).thenReturn(5);
        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        service.cleanupOldChatMemories();

        verify(jdbc).update(anyString(), dateCaptor.capture());
        LocalDateTime capturedDate = dateCaptor.getValue();
        LocalDateTime expectedDate = LocalDateTime.now().minusDays(3);
        
        assertThat(capturedDate).isAfter(expectedDate.minusMinutes(1))
                                .isBefore(expectedDate.plusMinutes(1));
    }
}
