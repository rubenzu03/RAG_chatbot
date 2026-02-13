package com.rubenzu03.rag_chatbot.rag.modules.preretrieve;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class QueryTransformerModule {

    private final ChatMemory chatMemory;
    private final QueryTransformer queryTransformer;

    private static final PromptTemplate COMPRESSION_PROMPT = new PromptTemplate("""
            Dado el historial de conversación y la consulta actual del usuario, reformula la consulta
            para que sea autocontenida y clara, incluyendo cualquier contexto relevante del historial.
            
            Enfócate en temas de programación, ingeniería del software y arquitectura del software.
            Si la consulta hace referencia a algo mencionado anteriormente, inclúyelo explícitamente.
            
            Historial de Conversación:
            {history}
            
            Consulta Actual:
            {query}
            
            Consulta Reformulada:
            """);

    @Autowired
    public QueryTransformerModule(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
        this.queryTransformer = CompressionQueryTransformer.builder()
                .promptTemplate(COMPRESSION_PROMPT)
                .chatClientBuilder(chatClientBuilder)
                .build();
    }

    public Query transformQuery(String rawQuery, String sessionId) {
        List<Message> historyMessages = getConversationHistory(sessionId);

        Query query = Query.builder()
                .text(rawQuery)
                .history(historyMessages)
                .build();

        return queryTransformer.transform(query);
    }

    private List<Message> getConversationHistory(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Collections.emptyList();
        }
        return chatMemory.get(sessionId);
    }
}
