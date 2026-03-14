package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.ChatMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatMemoryService {

    private final ChatMemoryRepository chatMemoryRepository;

    @Autowired
    public ChatMemoryService(ChatMemoryRepository chatMemoryRepository) {
        this.chatMemoryRepository = chatMemoryRepository;
    }
    public void deleteAll() {
        chatMemoryRepository.deleteAll();
    }
}
