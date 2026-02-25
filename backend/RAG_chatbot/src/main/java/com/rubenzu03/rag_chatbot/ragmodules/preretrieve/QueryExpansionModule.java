package com.rubenzu03.rag_chatbot.ragmodules.preretrieve;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryExpansionModule {

    private static final int DEFAULT_NUMBER_OF_QUERIES = 3;

    private final QueryExpander queryExpander;

    public QueryExpansionModule(ChatClient.Builder chatClientBuilder) {
        this.queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(chatClientBuilder)
                .numberOfQueries(DEFAULT_NUMBER_OF_QUERIES)
                .build();
    }

    public List<Query> expandQueries(Query query) {
        return queryExpander.expand(query);
    }
}
