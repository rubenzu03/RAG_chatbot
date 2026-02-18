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


    @GetMapping("/api/ai/debug/search")
    public ResponseEntity<Map<String, Object>> debugSearch(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "topK", defaultValue = "20") int topK,
            @RequestParam(name = "threshold", defaultValue = "0.0") double threshold) {

        List<Document> results = documentSearchModule.retrieveDocuments(
                new Query(query), topK, threshold);

        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("topK", topK);
        response.put("threshold", threshold);
        response.put("resultsCount", results.size());
        response.put("results", results.stream()
                .map(doc -> {
                    Map<String, Object> docMap = new HashMap<>();
                    docMap.put("score", doc.getScore());
                    docMap.put("content", truncate(doc.getFormattedContent(), 2000));
                    docMap.put("metadata", doc.getMetadata());
                    return docMap;
                })
                .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        String cleaned = text.replaceAll("\\s+", " ").trim();
        return cleaned.length() > maxLength
                ? cleaned.substring(0, maxLength) + "..."
                : cleaned;
    }
}
