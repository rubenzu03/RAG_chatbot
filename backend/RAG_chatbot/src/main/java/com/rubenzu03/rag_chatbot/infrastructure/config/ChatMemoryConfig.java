package com.rubenzu03.rag_chatbot.infrastructure.config;

import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.chat.EncryptedChatMemory;
import com.rubenzu03.rag_chatbot.infrastructure.security.ChatHistoryEncryptionService;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository, ChatHistoryEncryptionService encryptionService) {
        ChatMemory windowMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(10)
                .build();
        return new EncryptedChatMemory(windowMemory, encryptionService);
    }
}
