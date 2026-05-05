package com.rubenzu03.rag_chatbot.infrastructure.components;

import com.rubenzu03.rag_chatbot.application.service.ChatMemoryCleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ChatMemoryCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ChatMemoryCleanupScheduler.class);

    private final ChatMemoryCleanupService chatMemoryCleanupService;

    public ChatMemoryCleanupScheduler(ChatMemoryCleanupService chatMemoryCleanupService) {
        this.chatMemoryCleanupService = chatMemoryCleanupService;
    }

    @Scheduled(cron = "${app.chat-memory.cleanup-schedule:0 0 2 * * ?}")
    public void scheduledChatMemoryCleanup() {
        logger.info("Starting scheduled chat memory cleanup task");
        try {
            int deletedRows = chatMemoryCleanupService.cleanupOldChatMemories();
            logger.info("Scheduled cleanup completed. Deleted {} entries", deletedRows);
        } catch (Exception e) {
            logger.error("Error during scheduled chat memory cleanup", e);
        }
    }

    public int triggerManualCleanup() {
        logger.info("Manual chat memory cleanup triggered");
        return chatMemoryCleanupService.cleanupOldChatMemories();
    }
}
