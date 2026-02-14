package com.rubenzu03.rag_chatbot.rag.modules.postretrieve;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentPostProcessingModule {

    private static final Logger log = LoggerFactory.getLogger(DocumentPostProcessingModule.class);

    public List<Document> rankAndFilterDocuments(List<Document> documents,
                                                  double similarityThreshold, int topK) {
        log.info("Starting document ranking with {} documents, threshold={}, topK={}",
                 documents != null ? documents.size() : 0, similarityThreshold, topK);

        if (documents == null || documents.isEmpty()) {
            log.warn("No documents to rank - returning empty list");
            return Collections.emptyList();
        }

        int initialCount = documents.size();

        Map<String, Document> uniqueDocs = new LinkedHashMap<>();
        for (Document doc : documents) {
            String docId = extractDocumentId(doc);
            if (!uniqueDocs.containsKey(docId) ||
                getScore(doc) > getScore(uniqueDocs.get(docId))) {
                uniqueDocs.put(docId, doc);
            }
        }

        log.debug("Deduplication: {} docs → {} unique docs", initialCount, uniqueDocs.size());

        List<Document> result = uniqueDocs.values().stream()
                .sorted((d1, d2) -> {
                    double s1 = getScore(d1);
                    double s2 = getScore(d2);
                    return Double.compare(s2, s1);
                })
                .filter(doc -> getScore(doc) >= similarityThreshold)
                .limit(topK)
                .collect(Collectors.toList());

        log.info("Ranking complete: {} docs after threshold={} and top-{}",
                 result.size(), similarityThreshold, topK);

        return result;
    }

    private String extractDocumentId(Document doc) {
        Map<String, Object> metadata = doc.getMetadata();

        Object parentId = metadata.get("parent_document_id");
        if (parentId != null) {
            Object chunkIndex = metadata.get("chunk_index");
            if (chunkIndex != null) {
                return parentId + "_" + chunkIndex;
            }
            return parentId.toString();
        }

        String content = doc.getFormattedContent();
        return String.valueOf(content.hashCode());
    }

    private double getScore(Document doc) {
        Double score = doc.getScore();
        return (score != null) ? score : 0.0;
    }
}
