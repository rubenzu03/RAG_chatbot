package com.rubenzu03.rag_chatbot.rag.modules.postretrieve;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentPostProcessingModule {

    public List<Document> rankAndFilterDocuments(List<Document> documents, Query query,
                                                  double similarityThreshold, int topK) {
        if (documents == null || documents.isEmpty()) {
            return Collections.emptyList();
        }

        String queryText = query.text().toLowerCase();
        String[] queryTerms = queryText.split("\\s+");

        return documents.stream()
                .filter(doc -> {
                    Double score = doc.getScore();
                    if (score != null && score < similarityThreshold) {
                        return false;
                    }

                    String content = doc.getFormattedContent().toLowerCase();
                    for (String term : queryTerms) {
                        if (content.contains(term)) {
                            return true;
                        }
                    }
                    return false;
                })
                .limit(topK)
                .collect(Collectors.toList());
    }
}
