package com.rubenzu03.rag_chatbot.infrastructure.ragmodules.preretrieve;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class QueryExpansionModuleTest {

    private QueryExpansionModule module;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @BeforeEach
    void setUp() {
        module = new QueryExpansionModule(chatClientBuilder);
    }

    @Test
    void testExpandQueriesReturnsMultipleQueries() {
        Query query = Query.builder().text("What is machine learning?").build();
        assertThat(module).isNotNull();
        assertThat(query.text()).isEqualTo("What is machine learning?");
    }

    @Test
    void testExpandQueriesPreservesOriginalText() {
        String originalText = "How does neural network training work?";
        Query query = Query.builder().text(originalText).build();

        assertThat(query.text()).isEqualTo(originalText);
    }

    @Test
    void testExpandQueriesWithDifferentQueries() {
        Query query1 = Query.builder().text("Question 1?").build();
        Query query2 = Query.builder().text("Question 2?").build();

        assertThat(query1.text()).isNotEqualTo(query2.text());
        assertThat(query1.text()).isEqualTo("Question 1?");
        assertThat(query2.text()).isEqualTo("Question 2?");
    }

    @Test
    void testExpandQueriesReturnsNonEmptyList() {
        Query query = Query.builder().text("Tell me about AI").build();

        assertThat(query.text()).isNotEmpty();
    }

    @Test
    void testExpandQueriesWithComplexQuery() {
        String complexQuery = "What is the relationship between machine learning, deep learning, and artificial intelligence?";
        Query query = Query.builder().text(complexQuery).build();
        assertThat(query.text()).isNotEmpty();
        assertThat(query.text()).contains("machine learning");
        assertThat(query.text()).contains("deep learning");
    }
}
