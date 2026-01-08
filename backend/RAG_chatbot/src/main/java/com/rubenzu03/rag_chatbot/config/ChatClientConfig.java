package com.rubenzu03.rag_chatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    private static final String DEFAULT_SYSTEM_PROMPT = """
            You are a helpful AI assistant. Your purpose is to assist users with their questions
            and provide accurate, clear, and concise responses. Be professional, friendly, and
            informative in your interactions.
            """;

    @Bean
    public ChatClient llama3ChatClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .build();
    }

    @Bean
    public ChatClient gemmaChatClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(DEFAULT_SYSTEM_PROMPT)
                .build();
    }

}
