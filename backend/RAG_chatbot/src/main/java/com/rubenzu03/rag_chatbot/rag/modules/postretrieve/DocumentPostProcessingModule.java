package com.rubenzu03.rag_chatbot.rag.modules.postretrieve;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentPostProcessingModule {

    @Autowired
    private VectorStore vectorStore;

    /**
     * Ranks and filters documents based on similarity threshold and removes duplicates
     * @param documents List of documents to process
     * @param query The query used for retrieval
     * @param similarityThreshold Minimum similarity score (0.0 to 1.0)
     * @return Filtered and ranked list of documents
     */
    public List<Document> rankAndFilterDocuments(List<Document> documents, Query query, double similarityThreshold) {
        if (documents == null || documents.isEmpty()) {
            return Collections.emptyList();
        }

        // Filter by similarity threshold and remove duplicates
        Set<String> seenContent = new HashSet<>();

        return documents.stream()
                // Filter by similarity threshold
                .filter(doc -> {
                    Double score = doc.getScore();
                    return score != null && score >= similarityThreshold;
                })
                // Remove duplicate content
                .filter(doc -> {
                    String content = doc.getFormattedContent();
                    if (seenContent.contains(content)) {
                        return false;
                    }
                    seenContent.add(content);
                    return true;
                })
                // Sort by score (descending - highest score first)
                .sorted((doc1, doc2) -> {
                    Double score1 = doc1.getScore();
                    Double score2 = doc2.getScore();
                    if (score1 == null && score2 == null) return 0;
                    if (score1 == null) return 1;
                    if (score2 == null) return -1;
                    return Double.compare(score2, score1); // Descending order
                })
                .collect(Collectors.toList());
    }

    /**
     * Ranks and filters documents with a default similarity threshold of 0.7
     * @param documents List of documents to process
     * @param query The query used for retrieval
     * @return Filtered and ranked list of documents
     */
    public List<Document> rankAndFilterDocuments(List<Document> documents, Query query) {
        return rankAndFilterDocuments(documents, query, 0.7);
    }

    /**
     * Limits the number of documents returned after ranking and filtering
     * @param documents List of documents to process
     * @param query The query used for retrieval
     * @param similarityThreshold Minimum similarity score (0.0 to 1.0)
     * @param topK Maximum number of documents to return
     * @return Filtered, ranked, and limited list of documents
     */
    public List<Document> rankAndFilterDocuments(List<Document> documents, Query query, double similarityThreshold, int topK) {
        List<Document> rankedDocs = rankAndFilterDocuments(documents, query, similarityThreshold);
        return rankedDocs.stream()
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * Re-ranks documents based on metadata relevance or custom scoring
     * @param documents List of documents to rerank
     * @param query The query used for retrieval
     * @return Re-ranked list of documents
     */
    public List<Document> rerankByMetadata(List<Document> documents, Query query) {
        if (documents == null || documents.isEmpty()) {
            return Collections.emptyList();
        }

        // Rerank based on metadata priority (e.g., document type, recency, etc.)
        return documents.stream()
                .sorted((doc1, doc2) -> {
                    Double score1 = doc1.getScore();
                    Double score2 = doc2.getScore();

                    // Primary sort by score
                    if (score1 != null && score2 != null && !score1.equals(score2)) {
                        return Double.compare(score2, score1);
                    }

                    // Secondary sort: prioritize documents with more metadata
                    Map<String, Object> metadata1 = doc1.getMetadata();
                    Map<String, Object> metadata2 = doc2.getMetadata();
                    int metadataSize1 = metadata1.size();
                    int metadataSize2 = metadata2.size();

                    return Integer.compare(metadataSize2, metadataSize1);
                })
                .collect(Collectors.toList());
    }
}
