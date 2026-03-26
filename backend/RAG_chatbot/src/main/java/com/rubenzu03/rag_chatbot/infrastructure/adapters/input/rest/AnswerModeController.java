package com.rubenzu03.rag_chatbot.infrastructure.adapters.input.rest;

import com.rubenzu03.rag_chatbot.domain.dto.ChatResponse;
import com.rubenzu03.rag_chatbot.application.service.AnswerModeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class AnswerModeController {
    private final AnswerModeService answerModeService;

    @Autowired
    public AnswerModeController(AnswerModeService answerModeService) {
        this.answerModeService = answerModeService;
    }

    private String getAuthenticatedUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PostMapping("/api/ai/simplequery")
    public ResponseEntity<ChatResponse> askQuery(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "conversationId", required = false) String conversationId){

        String userId = getAuthenticatedUserEmail();
        String conversationKey = answerModeService.buildConversationKey(userId, conversationId);
        String response = answerModeService.answerSimpleQuery(query, conversationKey);

        return ResponseEntity.ok(new ChatResponse(response, conversationKey));
    }

    @PostMapping(value = "/api/ai/ragquery", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askQueryUsingRAG(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "conversationId", required = false) String conversationId) {

        String userId = getAuthenticatedUserEmail();
        String conversationKey = answerModeService.buildConversationKey(userId, conversationId);

        return answerModeService.AnswerWithRagQuery(query, conversationKey);
    }

}
