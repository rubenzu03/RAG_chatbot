package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.application.ports.input.ChatUseCase;
import com.rubenzu03.rag_chatbot.application.ports.output.ChatMemoryPort;
import com.rubenzu03.rag_chatbot.domain.dto.ChatHistoryMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMemoryService implements ChatUseCase {

    private final ChatMemoryPort chatMemoryPort;

    @Autowired
    public ChatMemoryService(ChatMemoryPort chatMemoryPort) {
        this.chatMemoryPort = chatMemoryPort;
    }
    public void deleteAll() {
        chatMemoryPort.deleteAllHistory();
    }

    @Override
    public List<ChatHistoryMessage> getHistory(String userId) {
        return chatMemoryPort.getHistory(userId);
    }

    @Override
    public void deleteHistory(String userId) {
        chatMemoryPort.deleteHistoryById(userId);
    }

    @Override
    public void deleteAllHistory() {
        chatMemoryPort.deleteAllHistory();
    }
}
