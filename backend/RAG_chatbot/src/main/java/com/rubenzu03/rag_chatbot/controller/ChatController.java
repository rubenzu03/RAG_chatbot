package com.rubenzu03.rag_chatbot.controller;

import com.rubenzu03.rag_chatbot.dto.ChatResponse;
import com.rubenzu03.rag_chatbot.persistence.ChatMemoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatController {

    private final ChatMemoryRepository chatMemoryRepository;

    public ChatController(ChatMemoryRepository chatMemoryRepository) {
        this.chatMemoryRepository = chatMemoryRepository;
    }

    @GetMapping("/api/ai/chat/{sessionId}")
    public List<ChatResponse> getChatResponsesBySessionId(@PathVariable String sessionId){
        return chatMemoryRepository.getChatHistoryBySessionId(sessionId);
    }
}
