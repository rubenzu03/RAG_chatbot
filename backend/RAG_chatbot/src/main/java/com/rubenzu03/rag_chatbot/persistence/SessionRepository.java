package com.rubenzu03.rag_chatbot.persistence;

import com.rubenzu03.rag_chatbot.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

@Repository
public interface SessionRepository extends JpaRepository<Session, String> {

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM spring_ai_chat_memory WHERE conversation_id = :sessionId", nativeQuery = true)
    void deleteChatMemoryBySessionId(@Param("sessionId") String sessionId);

    @Modifying
    @Transactional
    @SuppressWarnings("'Delete' statement without 'where' clears all data in the table ")
    @Query(value = "DELETE FROM spring_ai_chat_memory", nativeQuery = true)
    void deleteAllChatMemory();
}
