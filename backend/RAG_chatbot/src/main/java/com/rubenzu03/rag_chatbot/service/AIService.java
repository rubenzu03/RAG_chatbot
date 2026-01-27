package com.rubenzu03.rag_chatbot.service;

import com.rubenzu03.rag_chatbot.config.ChatClientConfig;
import com.rubenzu03.rag_chatbot.rag.modules.postretrieve.DocumentPostProcessingModule;
import com.rubenzu03.rag_chatbot.rag.modules.preretrieve.QueryExpansionModule;
import com.rubenzu03.rag_chatbot.rag.modules.preretrieve.QueryTransformerModule;
import com.rubenzu03.rag_chatbot.rag.modules.preretrieve.RewriteQueryModule;
import com.rubenzu03.rag_chatbot.rag.modules.preretrieve.TranslationQueryModule;
import com.rubenzu03.rag_chatbot.rag.modules.retrieve.DocumentJoinModule;
import com.rubenzu03.rag_chatbot.rag.modules.retrieve.DocumentSearchModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIService {

    private static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";

    private final ChatClient chatClient;

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    private final QueryTransformerModule queryTransformerModule;
    private final RewriteQueryModule rewriteQueryModule;
    private final TranslationQueryModule translationQueryModule;
    private final QueryExpansionModule queryExpansionModule;
    private final DocumentSearchModule documentSearchModule;
    private final DocumentJoinModule documentJoinModule;
    private final DocumentPostProcessingModule documentPostProcessingModule;


    @Autowired
    public AIService(@Qualifier("llama3ChatClient") ChatClient chatClient,
                     TranslationQueryModule translationQueryModule,
                     RewriteQueryModule rewriteQueryModule, QueryTransformerModule queryTransformerModule, QueryExpansionModule queryExpansionModule,
                     DocumentSearchModule documentSearchModule,
                     DocumentPostProcessingModule documentPostProcessingModule, DocumentJoinModule documentJoinModule) {
        this.chatClient = chatClient;
        this.translationQueryModule = translationQueryModule;
        this.rewriteQueryModule = rewriteQueryModule;
        this.queryTransformerModule = queryTransformerModule;
        this.queryExpansionModule = queryExpansionModule;
        this.documentSearchModule = documentSearchModule;
        this.documentPostProcessingModule = documentPostProcessingModule;
        this.documentJoinModule = documentJoinModule;
    }

    public String simpleQueryTest(String query, String sessionId){
        return this.chatClient.prompt(query)
                .advisors(advisor -> advisor.param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId))
                .call()
                .content();
    }

    public Flux<String> RAGQueryTest(String query, String sessionId) {
        // Step 1: Pre-retrieval - Query transformation pipeline with chat history
        Query finalQuery = queryTransformerModule.transformQuery(query, sessionId);
        finalQuery = rewriteQueryModule.rewriteUserQuery(finalQuery.text());
        finalQuery = translationQueryModule.translateQuery(finalQuery.text());

        // Step 2: Query expansion - Generate multiple query variations
        List<Query> expandedQueries = queryExpansionModule.expandQueries(finalQuery);

        if (!expandedQueries.contains(finalQuery)) {
            List<Query> allQueries = new ArrayList<>(expandedQueries);
            allQueries.addFirst(finalQuery);
            expandedQueries = allQueries;
        }

        Map<Query, List<List<Document>>> queryToDocuments = new HashMap<>();

        for (Query expandedQuery : expandedQueries) {

            List<Document> retrievedDocs = documentSearchModule.retrieveDocuments(expandedQuery, 10, 0.7);
            // Retrieve documents for each expanded query using similarity search
            /*List<Document> retrievedDocs = vectordb.similaritySearch(
                SearchRequest.builder()
                    .query(expandedQuery.text())
                    .topK(10)  // Retrieve top 10 for each query
                    .build()
            );
*/
            // Store results - wrap in a List<List<Document>> as expected by DocumentJoiner
            queryToDocuments.put(expandedQuery, List.of(retrievedDocs));
        }

        // Step 4: Join - Merge documents from all queries
        List<Document> joinedDocs = documentJoinModule.joinDocuments(queryToDocuments);

        // Step 5: Post-retrieve - Rank, filter, and deduplicate
        List<Document> rankedDocs = documentPostProcessingModule.rankAndFilterDocuments(
                joinedDocs,
                finalQuery,  // Use original final query for ranking
                0.7,         // Similarity threshold
                5            // Top K documents
        );

        if (rankedDocs.isEmpty()){
            log.warn("No documents found for query: {}", query);
        }

        // Construct context from ranked documents
        String context = rankedDocs.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n"));

        return chatClient.prompt()
                .system(ChatClientConfig.DEFAULT_SYSTEM_PROMPT)
                .user(u -> u
                    .text("Context:\n{context}\n\nQuestion: {question}")
                    .param("context", context)
                    .param("question", query))
                .advisors(advisor -> advisor.param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId))
                .stream()
                .content();
    }




}
