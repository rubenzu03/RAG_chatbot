package com.rubenzu03.rag_chatbot.config;

import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaModels {

    public OllamaChatModel ollamaLlamaModel;
    public OllamaChatModel ollamaCodeLlamaModel;
    public OllamaChatModel ollamaDeepseekModel;

    public OllamaModels() {
        OllamaApi ollamaApi = OllamaApi.builder().build();
        this.ollamaLlamaModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(
                        OllamaChatOptions.builder()
                                .model(OllamaModel.LLAMA3_1)
                                .temperature(0.0)
                                .build())
                .build();

        this.ollamaCodeLlamaModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaChatOptions.builder().model(OllamaModel.CODELLAMA)
                        .temperature(0.0).build())
                .build();

        this.ollamaDeepseekModel = OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(
                        OllamaChatOptions.builder()
                                .model("DEEPSEEK-R1")
                                .temperature(0.0)
                                .build())
                .build();
    }
}
