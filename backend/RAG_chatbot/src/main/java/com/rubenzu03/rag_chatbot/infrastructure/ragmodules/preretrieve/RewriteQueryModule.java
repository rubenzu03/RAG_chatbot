package com.rubenzu03.rag_chatbot.infrastructure.ragmodules.preretrieve;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Service;

@Service
public class RewriteQueryModule {

    private final QueryTransformer queryTransformer;

    public RewriteQueryModule(ChatClient.Builder chatClientBuilder) {
        this.queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .build();
    }

    public Query rewriteUserQuery(String compressedQuery) {
        Query query = new Query(compressedQuery);

        return queryTransformer.transform(query);
    }
}
