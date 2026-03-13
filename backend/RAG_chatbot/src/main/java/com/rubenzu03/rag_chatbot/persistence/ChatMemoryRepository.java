package com.rubenzu03.rag_chatbot.persistence;

import com.rubenzu03.rag_chatbot.dto.ChatResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChatMemoryRepository {
    private final JdbcTemplate jdbc;

    public ChatMemoryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void deleteByUserId(String userId) {
        jdbc.update("DELETE FROM spring_ai_chat_memory WHERE conversation_id = ?", userId);
    }

    @SuppressWarnings("'Delete' statement without 'where' clears all data in the table ")
    public void deleteAll() {
        jdbc.update("DELETE FROM spring_ai_chat_memory");
    }

    public List<ChatResponse> getChatHistoryByUserId(String userId){
        return jdbc.query("SELECT * FROM spring_ai_chat_memory WHERE conversation_id = ?",
                (rs, rowNum) -> new ChatResponse(rs.getString("response"), rs.getString("conversation_id")),
                userId);
    }
}
