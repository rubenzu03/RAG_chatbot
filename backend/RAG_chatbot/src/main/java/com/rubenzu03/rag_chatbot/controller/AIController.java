package com.rubenzu03.rag_chatbot.controller;

import com.rubenzu03.rag_chatbot.domain.Session;
import com.rubenzu03.rag_chatbot.dto.ChatResponse;
import com.rubenzu03.rag_chatbot.rag.modules.retrieve.DocumentSearchModule;
import com.rubenzu03.rag_chatbot.service.AnswerModeService;
import com.rubenzu03.rag_chatbot.service.SessionService;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AIController {
    private final AnswerModeService answerModeService;
    private final SessionService sessionService;
    private final DocumentSearchModule documentSearchModule;

    @Autowired
    public AIController(AnswerModeService answerModeService, SessionService sessionService,
                        DocumentSearchModule documentSearchModule) {
        this.answerModeService = answerModeService;
        this.sessionService = sessionService;
        this.documentSearchModule = documentSearchModule;
    }

    @PostMapping("/api/ai/test")
    public ResponseEntity<ChatResponse> askQuery(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "sessionId", required = false) String sessionId) {

        Session session = sessionService.getOrCreateSession(sessionId);
        String actualSessionId = session.getSessionId();

        String response = answerModeService.answerSimpleQuery(query, actualSessionId);

        return ResponseEntity.ok(new ChatResponse(response, actualSessionId));
    }

    @PostMapping(value = "/api/ai/test/ragquery", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askQueryUsingRAG(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "sessionId", required = false) String sessionId) {

        Session session = sessionService.getOrCreateSession(sessionId);
        String actualSessionId = session.getSessionId();

        return Flux.concat(
                Flux.just("[SESSION:" + actualSessionId + "]"),
                answerModeService.AnswerWithRagQuery(query, actualSessionId)
        );
    }

}
