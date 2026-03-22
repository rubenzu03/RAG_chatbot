package com.rubenzu03.rag_chatbot.infrastructure.adapters.input.rest;

import com.rubenzu03.rag_chatbot.application.ports.input.ChatUseCase;
import com.rubenzu03.rag_chatbot.domain.dto.ChatResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ChatController {

    private final ChatUseCase chatUseCase;

    public ChatController(ChatUseCase chatUseCase) {
        this.chatUseCase = chatUseCase;
    }

    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }


    @GetMapping("/api/ai/chat/history")
    public List<ChatResponse> getChatHistory(){
        String userId = getAuthenticatedUserEmail();
        return chatUseCase.getHistory(userId);
    }

    @DeleteMapping("/api/ai/chat/history")
    public void deleteChatHistory(){
        String userId = getAuthenticatedUserEmail();
        chatUseCase.deleteHistory(userId);
    }
}
