package com.rubenzu03.rag_chatbot.infrastructure.components;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RAGContextBuilderTest {

    private final RAGContextBuilder builder = new RAGContextBuilder();

    @Test
    void buildRAGContext_emptyListReturnsEmptyString() {
        assertThat(builder.buildRAGContext(List.of())).isEmpty();
    }

    @Test
    void buildRAGContext_stripsLeadingLine() {
        Document doc1 = new Document("Title\nBody");
        Document doc2 = new Document("Plain");

        String context = builder.buildRAGContext(List.of(doc1, doc2));

        assertThat(context).isEqualTo("Body\n\nPlain");
    }
}

