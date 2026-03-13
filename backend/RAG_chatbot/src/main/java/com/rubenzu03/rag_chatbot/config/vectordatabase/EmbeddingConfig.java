package com.rubenzu03.rag_chatbot.config.vectordatabase;

import com.knuddels.jtokkit.api.EncodingType;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingConfig {
    @Value("${spring.ai.embedding.batch.max-tokens:8000}")
    private int maxTokensPerBatch;

    @Value("${spring.ai.embedding.batch.reserve-percentage:0.1}")
    private double reservePercentage;

    @Bean
    public BatchingStrategy batchingStrategy() {
        return new TokenCountBatchingStrategy(
                EncodingType.CL100K_BASE,
                maxTokensPerBatch,
                reservePercentage
        );
    }
}
