package com.rubenzu03.rag_chatbot.rag.modules.preretrieve;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.stereotype.Service;

@Service
public class TranslationQueryModule {

    private static final String TARGET_LANGUAGE = "english";

    private final QueryTransformer queryTransformer;

    public TranslationQueryModule(ChatClient.Builder chatClientBuilder) {
        this.queryTransformer = TranslationQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .targetLanguage(TARGET_LANGUAGE)
                .build();
    }

    public Query translateQuery(String rewrittenQuery) {
        Query query = new Query(rewrittenQuery);

        return queryTransformer.transform(query);
    }

}
