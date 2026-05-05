package com.rubenzu03.rag_chatbot.infrastructure.config.vectordatabase;

import com.rubenzu03.rag_chatbot.application.service.IngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class IngestionRunnerTest {

    @Test
    void run_triggersDocumentIngestion() {
        IngestionService ingestionService = mock(IngestionService.class);
        ApplicationArguments args = mock(ApplicationArguments.class);
        IngestionRunner runner = new IngestionRunner(ingestionService);

        runner.run(args);

        verify(ingestionService, times(1)).ingestDocuments();
    }
}
