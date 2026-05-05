package com.rubenzu03.rag_chatbot.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.ollama.OllamaChatModel;

import static org.assertj.core.api.Assertions.assertThat;

class OllamaModelsTest {

    private final OllamaModels config = new OllamaModels();

    @Test
    void ollamaLlamaModel_createsBeanInstance() {
        OllamaChatModel model = config.ollamaLlamaModel();

        assertThat(model).isNotNull();
    }
}
