package com.rubenzu03.rag_chatbot.rag.modules.retrieve;

import com.rubenzu03.rag_chatbot.persistence.VectorDatabaseLoader;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentSearchModule {

    private final VectorStore vectorStore;
    private final VectorDatabaseLoader vectorDatabaseLoader;

    @Autowired
    public DocumentSearchModule(VectorDatabaseLoader vectorDatabaseLoader) {
        this.vectorDatabaseLoader = vectorDatabaseLoader;
        vectorStore = vectorDatabaseLoader.getVectorStore();
    }

    private List<Document> retrieveDocuments(Query query, int topK, double similarityThreshold){
        //TODO: Improve filter
        DocumentRetriever retriver = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(similarityThreshold)
                .topK(topK)
                .filterExpression(() -> new FilterExpressionBuilder()
                        .eq("test","test")
                        .build())
                .build();
        return retriver.retrieve(query);
    }


}
