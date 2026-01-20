package com.rubenzu03.rag_chatbot.controller;

import com.rubenzu03.rag_chatbot.domain.Session;
import com.rubenzu03.rag_chatbot.dto.ChatResponse;
import com.rubenzu03.rag_chatbot.service.AIService;
import com.rubenzu03.rag_chatbot.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AIController {
    private final AIService aiService;
    private final SessionService sessionService;

    @Autowired
    public AIController(AIService aiService, SessionService sessionService) {
        this.aiService = aiService;
        this.sessionService = sessionService;
    }

    @PostMapping("/api/ai/test")
    public ResponseEntity<ChatResponse> askQuery(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "sessionId", required = false) String sessionId) {

        Session session = sessionService.getOrCreateSession(sessionId);
        String actualSessionId = session.getSessionId();

        String response = aiService.simpleQueryTest(query, actualSessionId);

        return ResponseEntity.ok(new ChatResponse(response, actualSessionId));
    }

    @PostMapping("/api/ai/test/ragquery")
    public ResponseEntity<ChatResponse> askQueryUsingRAG(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "sessionId", required = false) String sessionId) {

        Session session = sessionService.getOrCreateSession(sessionId);
        String actualSessionId = session.getSessionId();

        String response = aiService.RAGQueryTest(query, actualSessionId);

        return ResponseEntity.ok(new ChatResponse(response, actualSessionId));
    }
}
