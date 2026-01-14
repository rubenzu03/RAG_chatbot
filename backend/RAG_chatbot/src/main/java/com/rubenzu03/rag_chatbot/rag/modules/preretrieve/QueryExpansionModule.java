package com.rubenzu03.rag_chatbot.rag.modules.preretrieve;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryExpansionModule {

    private final ChatClient.Builder chatClientBuilder;

    public QueryExpansionModule(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    public List<Query> expandQueries(String query){
        MultiQueryExpander queryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(chatClientBuilder)
                .numberOfQueries(10).build();

        return queryExpander.expand(new Query(query));
    }
}
