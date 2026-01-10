package com.rubenzu03.rag_chatbot.rag.modules;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Service;

@Service
public class RewriteQueryModule {


    private final ChatClient.Builder chatClientBuilder;

    public RewriteQueryModule(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    public Query rewriteUserQuery(String compressedQuery){
        Query query = new Query(compressedQuery);

        QueryTransformer queryTransformer = RewriteQueryTransformer.builder().
                chatClientBuilder(chatClientBuilder).build();

        return queryTransformer.transform(query);
    }
}
