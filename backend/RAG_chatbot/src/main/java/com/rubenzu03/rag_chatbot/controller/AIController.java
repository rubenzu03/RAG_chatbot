package com.rubenzu03.rag_chatbot.controller;

import com.rubenzu03.rag_chatbot.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AIController {
    private final AIService aiService;

    @Autowired
    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/api/ai/test")
    public ResponseEntity<String> askQuery(@RequestParam(name = "query") String query){
        String response = aiService.simpleQueryTest(query);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/ai/test/ragquery")
    public ResponseEntity<String> askQueryUsingRAG(@RequestParam(name="query") String query){
        String response = aiService.RAGQueryTest(query);
        return ResponseEntity.ok(response);
    }
}
