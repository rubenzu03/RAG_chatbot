package com.rubenzu03.rag_chatbot.infrastructure.ragmodules.retrieve;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DocumentSearchModuleTest {

    private DocumentSearchModule module;

    @Mock
    private VectorStore vectorStore;

    @BeforeEach
    void setUp() {
        module = new DocumentSearchModule(vectorStore);
    }

    @Test
    void testRetrieveDocumentsWithValidQuery() {
        Query query = Query.builder().text("What is RAG?").build();
        List<Document> expectedDocs = List.of(
                new Document("RAG is Retrieval Augmented Generation"),
                new Document("It improves model accuracy")
        );
        List<Document> result = module.retrieveDocuments(query, 5, 0.5);
        assertThat(result).isNotNull();
    }

    @Test
    void testRetrieveDocumentsWithDifferentTopK() {
        Query query = Query.builder().text("machine learning").build();
        List<Document> result5 = module.retrieveDocuments(query, 5, 0.5);
        List<Document> result10 = module.retrieveDocuments(query, 10, 0.5);

        assertThat(result5).isNotNull();
        assertThat(result10).isNotNull();
    }

    @Test
    void testRetrieveDocumentsWithDifferentThresholds() {
        Query query = Query.builder().text("neural networks").build();

        List<Document> result1 = module.retrieveDocuments(query, 5, 0.3);
        List<Document> result2 = module.retrieveDocuments(query, 5, 0.7);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
    }

    @Test
    void testRetrieveDocumentsWithSmallTopK() {
        Query query = Query.builder().text("test query").build();

        List<Document> result = module.retrieveDocuments(query, 1, 0.5);
        assertThat(result).isNotNull();
    }

    @Test
    void testRetrieveDocumentsWithLargeTopK() {
        Query query = Query.builder().text("comprehensive query").build();
        List<Document> result = module.retrieveDocuments(query, 100, 0.5);

        assertThat(result).isNotNull();
    }

    @Test
    void testRetrieveDocumentsWithZeroThreshold() {
        Query query = Query.builder().text("any query").build();
        List<Document> result = module.retrieveDocuments(query, 5, 0.0);

        assertThat(result).isNotNull();
    }

    @Test
    void testRetrieveDocumentsWithHighThreshold() {
        Query query = Query.builder().text("exact match query").build();
        List<Document> result = module.retrieveDocuments(query, 5, 0.9);

        assertThat(result).isNotNull();
    }
}
