package com.rubenzu03.rag_chatbot.components;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RAGContextBuilder {

    public String buildRAGContext(List<Document> documents) {
        if (documents.isEmpty()) return "";

        return documents.stream()
                .map(doc -> {
                    String formatted = doc.getFormattedContent();
                    int contentStart = formatted.indexOf('\n');
                    return contentStart > 0 ? formatted.substring(contentStart + 1).trim() : formatted;
                })
                .collect(Collectors.joining("\n\n"));
    }
}
