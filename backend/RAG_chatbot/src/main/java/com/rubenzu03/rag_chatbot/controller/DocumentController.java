package com.rubenzu03.rag_chatbot.controller;

import com.rubenzu03.rag_chatbot.service.RetrievalService;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DocumentController {

    RetrievalService retrievalService;


    @Autowired
    public DocumentController (RetrievalService retrievalService){
        this.retrievalService = retrievalService;
    }

    @GetMapping("/api/ai/debug/search")
    public ResponseEntity<Map<String, Object>> debugSearch(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "topK", defaultValue = "20") int topK) {

        List<Document> results = retrievalService.retrieveDocuments(
                new Query(query), topK);

        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("topK", topK);
        response.put("resultsCount", results.size());
        response.put("results", results.stream()
                .map(doc -> {
                    Map<String, Object> docMap = new HashMap<>();
                    docMap.put("score", doc.getScore());
                    docMap.put("content", truncate(doc.getFormattedContent()));
                    docMap.put("metadata", doc.getMetadata());
                    return docMap;
                })
                .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    private String truncate(String text) {
        if (text == null) return "";
        String cleaned = text.replaceAll("\\s+", " ").trim();
        return cleaned.length() > 2000
                ? cleaned.substring(0, 2000) + "..."
                : cleaned;
    }
}
