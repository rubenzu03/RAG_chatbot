package com.rubenzu03.rag_chatbot.rag;

import com.rubenzu03.rag_chatbot.VectorDatabaseLoader;
import com.rubenzu03.rag_chatbot.rag.modules.QueryExpansionModule;
import com.rubenzu03.rag_chatbot.rag.modules.QueryTransformerModule;
import com.rubenzu03.rag_chatbot.rag.modules.RewriteQueryModule;
import com.rubenzu03.rag_chatbot.rag.modules.TranslationQueryModule;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIService {

    private final ChatClient chatClient;
    private final ChatModel chatModel;
    private final VectorDatabaseLoader vectordb;



    private final QueryTransformerModule queryTransformerModule;
    private final RewriteQueryModule rewriteQueryModule;
    private final TranslationQueryModule translationQueryModule;
    private final QueryExpansionModule queryExpansionModule;

    private final String RAG_SYSTEM_PROMPT = """
            You are an advanced AI assistant that provides accurate and concise answers to user queries by leveraging relevant information from a provided context. 
            When answering, ensure that you only use the information available in the context. If the context does not contain sufficient information to answer the query, 
            respond with "I'm sorry, I don't have enough information to answer that question." Do not fabricate answers or use information outside of the provided context.
            """;


    @Autowired
    public AIService(@Qualifier("llama3ChatClient") ChatClient chatClient,
                     OllamaChatModel chatModel, VectorDatabaseLoader vectorDatabase, TranslationQueryModule translationQueryModule,
                     RewriteQueryModule rewriteQueryModule, QueryTransformerModule queryTransformerModule, QueryExpansionModule queryExpansionModule) {
        this.chatClient = chatClient;
        this.chatModel = chatModel;
        this.vectordb = vectorDatabase;
        this.translationQueryModule = translationQueryModule;
        this.rewriteQueryModule = rewriteQueryModule;
        this.queryTransformerModule = queryTransformerModule;
        this.queryExpansionModule = queryExpansionModule;
    }

    public String simpleQueryTest(String query){
        return this.chatClient.prompt(query).call().content();
    }

    public String RAGQueryTest(String query) {
        Query finalQuery = queryTransformerModule.transformQuery(query);
        finalQuery = rewriteQueryModule.rewriteUserQuery(finalQuery.text());
        finalQuery = translationQueryModule.translateQuery(finalQuery.text());
        List<Query> expandedQueries = queryExpansionModule.expandQueries(finalQuery.text());
        //TODO: Retrieve from vectordb
        return finalQuery.text();
    }




}
