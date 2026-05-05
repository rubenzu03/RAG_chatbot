package com.rubenzu03.rag_chatbot.infrastructure.components;

import com.rubenzu03.rag_chatbot.application.service.ChatMemoryCleanupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMemoryCleanupSchedulerTest {

    private ChatMemoryCleanupScheduler scheduler;

    @Mock
    private ChatMemoryCleanupService cleanupService;

    @BeforeEach
    void setUp() {
        scheduler = new ChatMemoryCleanupScheduler(cleanupService);
    }

    @Test
    void testScheduledChatMemoryCleanup_Success() {
        when(cleanupService.cleanupOldChatMemories()).thenReturn(5);
        scheduler.scheduledChatMemoryCleanup();

        verify(cleanupService).cleanupOldChatMemories();
    }

    @Test
    void testScheduledChatMemoryCleanup_NoRowsDeleted() {
        when(cleanupService.cleanupOldChatMemories()).thenReturn(0);
        scheduler.scheduledChatMemoryCleanup();

        verify(cleanupService).cleanupOldChatMemories();
    }

    @Test
    void testScheduledChatMemoryCleanup_MultipleRows() {
        when(cleanupService.cleanupOldChatMemories()).thenReturn(100);

        scheduler.scheduledChatMemoryCleanup();
        verify(cleanupService).cleanupOldChatMemories();
    }

    @Test
    void testScheduledChatMemoryCleanup_HandleException() {
        when(cleanupService.cleanupOldChatMemories())
                .thenThrow(new RuntimeException("Database error"));

     
        scheduler.scheduledChatMemoryCleanup();

        verify(cleanupService).cleanupOldChatMemories();
    }

    @Test
    void testTriggerManualCleanup_Success() {
        int expectedDeletedRows = 7;
        when(cleanupService.cleanupOldChatMemories()).thenReturn(expectedDeletedRows);

        int result = scheduler.triggerManualCleanup();

        assertThat(result).isEqualTo(expectedDeletedRows);
        verify(cleanupService).cleanupOldChatMemories();
    }

    @Test
    void testTriggerManualCleanup_NoRows() {
        when(cleanupService.cleanupOldChatMemories()).thenReturn(0);
        int result = scheduler.triggerManualCleanup();

        assertThat(result).isEqualTo(0);
        verify(cleanupService).cleanupOldChatMemories();
    }

    @Test
    void testTriggerManualCleanup_LargeNumber() {
        int expectedDeletedRows = 1000;
        when(cleanupService.cleanupOldChatMemories()).thenReturn(expectedDeletedRows);
        int result = scheduler.triggerManualCleanup();

        assertThat(result).isEqualTo(expectedDeletedRows);
        verify(cleanupService).cleanupOldChatMemories();
    }

    @Test
    void testSchedulerIsCreatedWithService() {
        ChatMemoryCleanupScheduler createdScheduler = new ChatMemoryCleanupScheduler(cleanupService);

        assertThat(createdScheduler).isNotNull();
    }

    @Test
    void testTriggerManualCleanup_ExceptionHandling() {
        when(cleanupService.cleanupOldChatMemories())
                .thenThrow(new RuntimeException("Service error"));

        try {
            scheduler.triggerManualCleanup();
        } catch (RuntimeException e) {
            assertThat(e).hasMessageContaining("Service error");
        }
    }
}
