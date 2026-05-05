package com.rubenzu03.rag_chatbot.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ChatMemoryCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(ChatMemoryCleanupService.class);

    private static final int DAYS_THRESHOLD = 3;
    private final JdbcTemplate jdbc;

    public ChatMemoryCleanupService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int cleanupOldChatMemories() {
        try {
            LocalDateTime thresholdDate = LocalDateTime.now().minusDays(DAYS_THRESHOLD);
            String formattedDate = thresholdDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String sql = "DELETE FROM spring_ai_chat_memory WHERE created_at < ?";
            int deletedRows = jdbc.update(sql, thresholdDate);

            if (deletedRows > 0) {
                logger.info("Successfully deleted {} old chat memory entries (older than {} days)", deletedRows,
                        DAYS_THRESHOLD);
            } else {
                logger.debug("No chat memory entries found older than {} days", DAYS_THRESHOLD);
            }

            return deletedRows;
        } catch (Exception e) {
            logger.error("Error occurred while cleaning up old chat memories", e);
            throw new RuntimeException("Failed to cleanup old chat memories", e);
        }
    }

    public int cleanupChatMemoriesByDays(int days) {
        try {
            LocalDateTime thresholdDate = LocalDateTime.now().minusDays(days);
            String sql = "DELETE FROM spring_ai_chat_memory WHERE created_at < ?";
            int deletedRows = jdbc.update(sql, thresholdDate);

            if (deletedRows > 0) {
                logger.info("Successfully deleted {} chat memory entries (older than {} days)", deletedRows, days);
            } else {
                logger.debug("No chat memory entries found older than {} days", days);
            }

            return deletedRows;
        } catch (Exception e) {
            logger.error("Error occurred while cleaning up chat memories by {} days", days, e);
            throw new RuntimeException("Failed to cleanup chat memories", e);
        }
    }

    public long countOldChatMemories() {
        try {
            LocalDateTime thresholdDate = LocalDateTime.now().minusDays(DAYS_THRESHOLD);
            String sql = "SELECT COUNT(*) FROM spring_ai_chat_memory WHERE created_at < ?";
            Long count = jdbc.queryForObject(sql, Long.class, thresholdDate);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.error("Error occurred while counting old chat memories", e);
            throw new RuntimeException("Failed to count old chat memories", e);
        }
    }
}
