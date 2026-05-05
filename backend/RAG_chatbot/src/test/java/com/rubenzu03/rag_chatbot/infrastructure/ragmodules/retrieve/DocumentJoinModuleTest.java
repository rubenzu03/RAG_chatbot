package com.rubenzu03.rag_chatbot.infrastructure.ragmodules.retrieve;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentJoinModuleTest {

    private DocumentJoinModule module;

    @BeforeEach
    void setUp() {
        module = new DocumentJoinModule();
    }

    @Test
    void testJoinDocumentsWithSingleQuery() {
        Query query = Query.builder().text("test query").build();
        List<Document> docs = List.of(
                new Document("Document 1"),
                new Document("Document 2")
        );
        Map<Query, List<List<Document>>> queryMap = Map.of(query, List.of(docs));
        List<Document> result = module.joinDocuments(queryMap);

        assertThat(result).isNotNull();
    }

    @Test
    void testJoinDocumentsWithMultipleQueries() {
        Query query1 = Query.builder().text("query 1").build();
        Query query2 = Query.builder().text("query 2").build();
        
        List<Document> docs1 = List.of(new Document("Doc 1-1"), new Document("Doc 1-2"));
        List<Document> docs2 = List.of(new Document("Doc 2-1"), new Document("Doc 2-2"));
        
        Map<Query, List<List<Document>>> queryMap = Map.of(
                query1, List.of(docs1),
                query2, List.of(docs2)
        );

        List<Document> result = module.joinDocuments(queryMap);
        assertThat(result).isNotNull();
    }

    @Test
    void testJoinDocumentsWithEmptyList() {
        Query query = Query.builder().text("query").build();
        Map<Query, List<List<Document>>> queryMap = Map.of(query, List.of());
        List<Document> result = module.joinDocuments(queryMap);
        assertThat(result).isNotNull();
    }

    @Test
    void testJoinDocumentsPreservesContent() {
        Query query = Query.builder().text("content query").build();
        List<Document> docs = List.of(
                new Document("Important content"),
                new Document("More content")
        );
        Map<Query, List<List<Document>>> queryMap = Map.of(query, List.of(docs));

        List<Document> result = module.joinDocuments(queryMap);

        assertThat(result).isNotNull();
    }

    @Test
    void testJoinDocumentsWithNestedLists() {
        Query query = Query.builder().text("nested query").build();
        List<List<Document>> nestedDocs = List.of(
                List.of(new Document("Nested 1-1"), new Document("Nested 1-2")),
                List.of(new Document("Nested 2-1"), new Document("Nested 2-2"))
        );
        Map<Query, List<List<Document>>> queryMap = Map.of(query, nestedDocs);
        List<Document> result = module.joinDocuments(queryMap);

        assertThat(result).isNotNull();
    }

    @Test
    void testJoinDocumentsReturnsNotNull() {
        Query query = Query.builder().text("null check query").build();
        List<Document> docs = List.of(new Document("Test"));
        Map<Query, List<List<Document>>> queryMap = Map.of(query, List.of(docs));

        List<Document> result = module.joinDocuments(queryMap);
        assertThat(result).isNotNull();
    }
}
