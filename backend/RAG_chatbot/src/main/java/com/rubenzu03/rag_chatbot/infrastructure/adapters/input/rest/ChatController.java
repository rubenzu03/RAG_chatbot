package com.rubenzu03.rag_chatbot.infrastructure.adapters.input.rest;

import com.rubenzu03.rag_chatbot.domain.dto.ChatResponse;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.ChatMemoryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ChatController {

    //TODO: Refactor this to use a service layer instead of directly accessing the repository from the controller

    private final ChatMemoryRepository chatMemoryRepository;

    public ChatController(ChatMemoryRepository chatMemoryRepository) {
        this.chatMemoryRepository = chatMemoryRepository;
    }

    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }


    @GetMapping("/api/ai/chat/history")
    public List<ChatResponse> getChatHistory(){
        String userId = getAuthenticatedUserEmail();
        return chatMemoryRepository.getChatHistoryByUserId(userId);
    }

    @DeleteMapping("/api/ai/chat/history")
    public void deleteChatHistory(){
        String userId = getAuthenticatedUserEmail();
        chatMemoryRepository.deleteByUserId(userId);
    }
}
