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

    public static final String QUESTION_MODE_GENERATION_PROMPT = """
              Eres un asistente de estudio.
              
              Tu tarea es:
              - Generar preguntas de estudio basadas ÚNICAMENTE en el contexto de los documentos proporcionados.
              - Evaluar respuestas del usuario comparándolas con ese contexto
              - Indicar claramente si la respuesta es correcta o incorrecta.
              - Explicar el porqué de forma clara
              
              Reglas:
              - No inventes información fuera del contexto
              - Si la información no aparece en el contexto, indícalo claramente
              - Sé conciso.
              - Usa un tono neutral, educativo y amigable
             
              """;

    @Bean
    public ChatClient AnswerModeChatClient(OllamaChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultSystem(ANSWER_MODE_GENERATION_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Bean
    public ChatClient QuestionModeChatClient(OllamaChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultSystem(QUESTION_MODE_GENERATION_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }


}
