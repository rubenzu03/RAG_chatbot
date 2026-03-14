package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.adapter;

import com.rubenzu03.rag_chatbot.application.ports.output.ChatMemoryPort;
import com.rubenzu03.rag_chatbot.domain.dto.ChatResponse;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.ChatMemoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatMemoryAdapter implements ChatMemoryPort {

    private final ChatMemoryRepository chatMemoryRepository;

    public ChatMemoryAdapter(ChatMemoryRepository chatMemoryRepository) {
        this.chatMemoryRepository = chatMemoryRepository;
    }

    @Override
    public void deleteAllHistory() {
        chatMemoryRepository.deleteAll();
    }

    @Override
    public void deleteHistoryById(String userId) {
        chatMemoryRepository.deleteByUserId(userId);
    }

    @Override
    public List<ChatResponse> getHistory(String userId) {
        return chatMemoryRepository.getChatHistoryByUserId(userId);
    }
}
