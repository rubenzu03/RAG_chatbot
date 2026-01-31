package com.rubenzu03.rag_chatbot.rag.modules.postretrieve;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentPostProcessingModule {

    private static final Set<String> STOP_WORDS = Set.of(
            "el", "la", "los", "las", "un", "una", "de", "del", "en", "que", "y", "a", "es", "por",
            "the", "an", "in", "on", "at", "to", "for", "of", "and", "is", "are", "with"
    );

    public List<Document> rankAndFilterDocuments(List<Document> documents, Query query,
                                                  double similarityThreshold, int topK) {
        if (documents == null || documents.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> queryTerms = extractQueryTerms(query.text());

        return documents.stream()
                .filter(doc -> {
                    Double score = doc.getScore();
                    return score == null || score >= similarityThreshold;
                })
                .sorted((d1, d2) -> {
                    Double s1 = d1.getScore();
                    Double s2 = d2.getScore();
                    if (s1 == null && s2 == null) return 0;
                    if (s1 == null) return 1;
                    if (s2 == null) return -1;
                    return Double.compare(s2, s1);
                })
                .filter(doc -> hasRelevantContent(doc, queryTerms))
                .limit(topK)
                .collect(Collectors.toList());
    }

    private Set<String> extractQueryTerms(String queryText) {
        return Arrays.stream(queryText.toLowerCase().split("\\s+"))
                .filter(term -> term.length() > 2)
                .filter(term -> !STOP_WORDS.contains(term))
                .collect(Collectors.toSet());
    }

    private boolean hasRelevantContent(Document doc, Set<String> queryTerms) {
        if (queryTerms.isEmpty()) {
            return true;
        }

        String content = doc.getFormattedContent().toLowerCase();
        return queryTerms.stream().anyMatch(content::contains);
    }
}
