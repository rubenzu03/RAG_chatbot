package com.rubenzu03.rag_chatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {


    public static final String ANSWER_MODE_GENERATION_PROMPT = """
            Eres un asistente de estudio para universitarios con IA. Tu objetivo es el de asistir, explicar y generar código
            relacionado con temas de programación, ingenieria del software, arquitectura del software.
            Tienes que ser profesional, amable e informal. Intenta ser lo mas objetivo posible. En caso de que te llegue un termino con varias definiciones,
            elige siempre la que este mas relacionada con la programación y la ingeniería del software.
            Si el usuario intenta realizar consultas con temas inapropiados, ofensivos o ilegales,
            debes rechazar la consulta de forma amable y educada y mencionar que solo eres un asistente de estudio.
            No menciones nunca donde se encuentran los archivos con los que has trabajado.
            """;

    public static final String QUESTION_GENERATION_PROMPT = """
            Contexto:
            %s
            
            Genera UNA pregunta de estudio clara y concreta basada únicamente en el contexto.
            La pregunta debe:
            - Evaluar comprensión conceptual, no memoria literal
            - Tener una respuesta verificable en el contexto
            
            Devuelve SOLO la pregunta, sin explicaciones adicionales.
            ""\";
            """;

    public static final String EVALUATION_PROMPT = """
            Contexto:
            %s
            
            Pregunta:
            %s
            
            Respuesta:
            %s
            
            Evalúa la respuesta siguiendo estas reglas:
            1. Indica primero si es CORRECTA o INCORRECTA.
            2. Explica el porqué basándote únicamente en el contexto.
            3. Si la respuesta es parcialmente correcta, indícalo claramente.
            4. No introduzcas información que no esté en el contexto.
            5. Si la respuesta recibida sugiere que el usuario no conoce la respuesta, evalúa la respuesta como INCORRECTA
            
            Formato de salida:
            - Resultado: CORRECTA | INCORRECTA | PARCIAL
            - Explicación: texto breve y claro
                        ""\";
            """;

    @Bean("AnswerModeChatClient")
    public ChatClient AnswerModeChatClient(OllamaChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultSystem(ANSWER_MODE_GENERATION_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Bean("QuestionModeChatClient")
    public ChatClient QuestionModeChatClient(OllamaChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel).build();
    }


}
