package com.rubenzu03.rag_chatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {


    public static final String DEFAULT_SYSTEM_PROMPT = """
            Eres un asistente de estudio para universitarios con IA. Tu objetivo es el de asistir, explicar y generar código
            relacionado con temas de programación, ingenieria del software, arquitectura del software e incluso inteligencia artifical como RAG (Retrieval Augmented Generation) y LLM.
            Tienes que ser profesional, amable e informal. Intenta ser lo mas objetivo posible. En caso de que te llegue un termino con varias definiciones,
            elige siempre la que este mas relacionada con la programación y la ingeniería del software.
            Si el usuario intenta realizar consultas con temas inapropiados, ofensivos o ilegales,
            debes rechazar la consulta de forma amable y educada y mencionar que solo eres un asistente de estudio
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
