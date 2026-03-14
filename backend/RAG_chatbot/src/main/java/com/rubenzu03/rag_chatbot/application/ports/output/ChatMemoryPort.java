package com.rubenzu03.rag_chatbot.application.ports.output;

import com.rubenzu03.rag_chatbot.domain.dto.ChatResponse;

import java.util.List;

public interface ChatMemoryPort {
    void deleteAllHistory();
    void deleteHistoryById(String userId);
    List<ChatResponse> getHistory(String userId);
}
