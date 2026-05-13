package com.rubenzu03.rag_chatbot.application.ports.output;

import com.rubenzu03.rag_chatbot.domain.dto.ChatHistoryMessage;

import java.util.List;

public interface ChatMemoryPort {
    void deleteAllHistory();
    void deleteHistoryById(String userId);
    List<ChatHistoryMessage> getHistory(String userId);
}
