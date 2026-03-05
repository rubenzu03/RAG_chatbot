package com.rubenzu03.rag_chatbot.controller;

import com.rubenzu03.rag_chatbot.dto.ChatResponse;
import com.rubenzu03.rag_chatbot.service.AnswerModeService;
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

    @PostMapping("/api/ai/test")
    public ResponseEntity<ChatResponse> askQuery(
            @RequestParam(name = "query") String query){

        String userId = getAuthenticatedUserEmail();
        String response = answerModeService.answerSimpleQuery(query, userId);

        return ResponseEntity.ok(new ChatResponse(response, userId));
    }

    @PostMapping(value = "/api/ai/test/ragquery", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askQueryUsingRAG(
            @RequestParam(name = "query") String query) {

        String userId = getAuthenticatedUserEmail();

        return answerModeService.AnswerWithRagQuery(query, userId);
    }

}
