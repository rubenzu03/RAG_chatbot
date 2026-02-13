package com.rubenzu03.rag_chatbot.rag.modules.retrieve;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentSearchModule {

    private static final int DEFAULT_TOP_K = 10;
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.7;

    private final VectorStore vectorStore;
    private final DocumentRetriever defaultRetriever;

    @Autowired
    public DocumentSearchModule(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.defaultRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(DEFAULT_SIMILARITY_THRESHOLD)
                .topK(DEFAULT_TOP_K)
                .build();
    }

    public List<Document> retrieveDocuments(Query query, int topK, double similarityThreshold) {
        if (topK == DEFAULT_TOP_K && similarityThreshold == DEFAULT_SIMILARITY_THRESHOLD) {
            return defaultRetriever.retrieve(query);
        }

        DocumentRetriever customRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(similarityThreshold)
                .topK(topK)
                .build();
        return customRetriever.retrieve(query);
    }

}
