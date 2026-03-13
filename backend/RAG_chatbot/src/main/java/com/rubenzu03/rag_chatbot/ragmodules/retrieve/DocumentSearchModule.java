package com.rubenzu03.rag_chatbot.ragmodules.retrieve;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class DocumentSearchModule {

    private static final Logger log = LoggerFactory.getLogger(DocumentSearchModule.class);

    @Value("${rag.search.default-top-k:15}")
    private int defaultTopK;

    @Value("${rag.search.default-similarity-threshold:0.5}")
    private double defaultSimilarityThreshold;

    private final VectorStore vectorStore;

    @Autowired
    public DocumentSearchModule(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public List<Document> retrieveDocuments(Query query, int topK, double similarityThreshold) {
        log.debug("Searching for query: '{}' with topK={}, threshold={}",
                query.text(), topK, similarityThreshold);

        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(similarityThreshold)
                .topK(topK)
                .build();

        return retriever.retrieve(query);
    }

}
