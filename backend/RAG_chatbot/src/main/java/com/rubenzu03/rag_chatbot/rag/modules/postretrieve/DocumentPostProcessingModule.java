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

        Map<String, Document> uniqueDocs = new LinkedHashMap<>();
        for (Document doc : documents) {
            String docId = getDocumentId(doc);
            Document existing = uniqueDocs.get(docId);
            if (existing == null || getScore(doc) > getScore(existing)) {
                uniqueDocs.put(docId, doc);
            }
        }

        List<Document> result = uniqueDocs.values().stream()
                .sorted(Comparator.comparingDouble(this::getScore).reversed())
                .filter(doc -> getScore(doc) >= similarityThreshold)
                .limit(topK)
                .collect(Collectors.toList());

        log.info("Post-processing: {} → {} unique → {} filtered (threshold={}, top={})",
                documents.size(), uniqueDocs.size(), result.size(), similarityThreshold, topK);

        return result;
    }

    private String getDocumentId(Document doc) {
        Map<String, Object> metadata = doc.getMetadata();
        Object parentId = metadata.get("parent_document_id");
        Object chunkIndex = metadata.get("chunk_index");

        if (parentId != null && chunkIndex != null) {
            return parentId + "_" + chunkIndex;
        }
        if (parentId != null) {
            return parentId.toString();
        }
        return String.valueOf(doc.getFormattedContent().hashCode());
    }

    private double getScore(Document doc) {
        return doc.getScore() != null ? doc.getScore() : 0.0;
    }
}
