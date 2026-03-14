package com.rubenzu03.rag_chatbot.application.ports.input;

import java.util.List;

public interface ChatUseCase {
    String sendMessage(String userId, String message);
    List<String> getHistory(String userId);
}

