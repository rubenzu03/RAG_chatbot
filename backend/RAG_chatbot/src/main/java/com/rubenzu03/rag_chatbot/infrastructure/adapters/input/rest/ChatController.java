package com.rubenzu03.rag_chatbot.infrastructure.adapters.input.rest;

import com.rubenzu03.rag_chatbot.application.ports.input.ChatUseCase;
import com.rubenzu03.rag_chatbot.domain.dto.ChatHistoryMessage;
import com.rubenzu03.rag_chatbot.domain.dto.ChatHistoryResponse;
import com.rubenzu03.rag_chatbot.domain.dto.ChatResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ChatController {

    private static final String DEFAULT_CONVERSATION_ID = "default";

    private final ChatUseCase chatUseCase;

    public ChatController(ChatUseCase chatUseCase) {
        this.chatUseCase = chatUseCase;
    }

    private String getAuthenticatedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }


    @GetMapping("/api/ai/chat/history")
    public ChatHistoryResponse getChatHistory(
            @RequestParam(name = "conversationId", required = false) String conversationId){
        String userId = getAuthenticatedUserEmail();
        String conversationKey = buildConversationKey(userId, conversationId);
        List<ChatHistoryMessage> history = chatUseCase.getHistory(conversationKey);
        return new ChatHistoryResponse(history);
    }

    @DeleteMapping("/api/ai/chat/history")
    public void deleteChatHistory(
            @RequestParam(name = "conversationId", required = false) String conversationId){
        String userId = getAuthenticatedUserEmail();
        String conversationKey = buildConversationKey(userId, conversationId);
        chatUseCase.deleteHistory(conversationKey);
    }

    private String buildConversationKey(String userId, String conversationId) {
        String safeUserId = (userId == null || userId.isBlank()) ? "anonymous" : userId.trim();
        String safeConversationId = (conversationId == null || conversationId.isBlank())
                ? DEFAULT_CONVERSATION_ID
                : conversationId.trim();
        return safeUserId + "::" + safeConversationId;
    }
}
