package com.rubenzu03.rag_chatbot.rag.modules.preretrieve;

import com.rubenzu03.rag_chatbot.config.ChatClientConfig;
import com.rubenzu03.rag_chatbot.dto.ChatResponse;
import com.rubenzu03.rag_chatbot.persistence.ChatMemoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryTransformerModule {

    private final ChatClient.Builder chatClientBuilder;

    @Autowired
    public QueryTransformerModule(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    public Query transformQuery(String rawQuery, String sessionId){
        Query query = Query.builder()
                .text(rawQuery)
                .build();

        PromptTemplate promptTemplate = new PromptTemplate("""
                Dado el historial de la conversación y la consulta actual, 
                comprime y reformula la consulta para que sea independiente y autocontenida.
                Recuerda que eres un asistente de estudio para universitarios con IA de asignaturas
                relacionadas con la informática y la ingenieria
            
    
                Conversation History:
                {history}
                
                Current Query:
                {query}
                Compressed Query:
                """);
        QueryTransformer queryTransformer = CompressionQueryTransformer.builder()
                .promptTemplate(promptTemplate)
                .chatClientBuilder(chatClientBuilder)
                .build();

        return queryTransformer.transform(query);
    }

}
