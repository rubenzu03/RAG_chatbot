package com.rubenzu03.rag_chatbot.rag.modules;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QueryTransformerModule {

    private final ChatClient.Builder chatClientBuilder;

    @Autowired
    public QueryTransformerModule(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    public Query transformQuery(String rawQuery){
        //TODO: add history and context
        Query query = Query.builder()
                .text(rawQuery).history().build();

        QueryTransformer queryTransformer = CompressionQueryTransformer.builder().chatClientBuilder(chatClientBuilder).build();

        return queryTransformer.transform(query);
    }
}
