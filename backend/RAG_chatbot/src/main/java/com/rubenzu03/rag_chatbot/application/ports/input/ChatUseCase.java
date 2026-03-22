package com.rubenzu03.rag_chatbot.application.ports.input;

import com.rubenzu03.rag_chatbot.domain.dto.ChatResponse;

import java.util.List;

public interface ChatUseCase {
    //String sendMessage(String userId, String message);
    List<ChatResponse> getHistory(String userId);
    void deleteHistory(String userId);
    void deleteAllHistory();
}

