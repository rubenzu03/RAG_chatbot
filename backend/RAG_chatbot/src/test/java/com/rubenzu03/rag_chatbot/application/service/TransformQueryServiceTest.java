package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.infrastructure.ragmodules.preretrieve.QueryTransformerModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.rag.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransformQueryServiceTest {

    private TransformQueryService service;

    @Mock
    private QueryTransformerModule queryTransformer;

    @BeforeEach
    void setUp() {
        service = new TransformQueryService(queryTransformer);
    }

    @Test
    void testTransformQueryWithValidQuery() {
        Query originalQuery = Query.builder().text("What is RAG?").build();
        Query transformedQuery = Query.builder().text("What is Retrieval Augmented Generation?").build();
        when(queryTransformer.transformQuery(anyString(), anyString()))
                .thenReturn(transformedQuery);

        Query result = service.transformQuery(originalQuery, "user123");
        assertThat(result.text()).isEqualTo("What is Retrieval Augmented Generation?");
    }

    @Test
    void testTransformQueryPreservesStructure() {
        // Arrange
        Query originalQuery = Query.builder()
                .text("How does clustering work?")
                .build();
        Query transformedQuery = Query.builder()
                .text("Explain clustering algorithms")
                .build();

        when(queryTransformer.transformQuery(anyString(), anyString()))
                .thenReturn(transformedQuery);

        // Act
        Query result = service.transformQuery(originalQuery, "user456");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.text()).isNotEmpty();
    }

    @Test
    void testTransformQueryWithDifferentUserId() {
        // Arrange
        Query query = Query.builder().text("test query").build();
        Query expected = Query.builder().text("transformed query").build();

        when(queryTransformer.transformQuery(anyString(), anyString()))
                .thenReturn(expected);

        // Act
        Query result = service.transformQuery(query, "different-user");

        // Assert
        assertThat(result.text()).isEqualTo("transformed query");
    }

    @Test
    void testTransformQueryWithComplexQuery() {
        // Arrange
        Query complexQuery = Query.builder()
                .text("How can I implement machine learning models with Python?")
                .build();
        Query simplifiedQuery = Query.builder()
                .text("Implement ML models Python")
                .build();

        when(queryTransformer.transformQuery(anyString(), anyString()))
                .thenReturn(simplifiedQuery);

        // Act
        Query result = service.transformQuery(complexQuery, "user789");

        // Assert
        assertThat(result.text()).isNotNull();
        assertThat(result.text()).contains("ML", "models");
    }
}
