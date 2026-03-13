package com.rubenzu03.rag_chatbot.config.vectordatabase;

import com.rubenzu03.rag_chatbot.service.IngestionService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnExpression("'${MINIO.ENDPOINT:${MINIO_ENDPOINT:}}' != ''")
public class IngestionRunner implements ApplicationRunner {

    private final IngestionService ingestionService;

    public IngestionRunner(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        ingestionService.ingestDocuments();
    }
}