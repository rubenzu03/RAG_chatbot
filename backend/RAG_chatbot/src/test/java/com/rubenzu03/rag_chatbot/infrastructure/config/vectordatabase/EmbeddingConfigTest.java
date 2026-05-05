package com.rubenzu03.rag_chatbot.infrastructure.config.vectordatabase;

import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class EmbeddingConfigTest {

    private EmbeddingConfig config = new EmbeddingConfig();

    @Test
    void testBatchingStrategyBeanCreation() {
        ReflectionTestUtils.setField(config, "maxTokensPerBatch", 8000);
        ReflectionTestUtils.setField(config, "reservePercentage", 0.1);
        BatchingStrategy strategy = config.batchingStrategy();

        assertThat(strategy).isNotNull();
    }

    @Test
    void testBatchingStrategyWithDefaultValues() {
        ReflectionTestUtils.setField(config, "maxTokensPerBatch", 8000);
        ReflectionTestUtils.setField(config, "reservePercentage", 0.1);

        BatchingStrategy strategy = config.batchingStrategy();
        assertThat(strategy).isNotNull();
    }

    @Test
    void testBatchingStrategyWithCustomValues() {
        ReflectionTestUtils.setField(config, "maxTokensPerBatch", 16000);
        ReflectionTestUtils.setField(config, "reservePercentage", 0.2);

        BatchingStrategy strategy = config.batchingStrategy();
        assertThat(strategy).isNotNull();
    }
}
