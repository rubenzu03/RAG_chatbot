package com.rubenzu03.rag_chatbot.service;

import com.rubenzu03.rag_chatbot.config.ChatClientConfig;
import com.rubenzu03.rag_chatbot.rag.modules.postretrieve.DocumentPostProcessingModule;
import com.rubenzu03.rag_chatbot.rag.modules.preretrieve.QueryExpansionModule;
import com.rubenzu03.rag_chatbot.rag.modules.preretrieve.QueryTransformerModule;
import com.rubenzu03.rag_chatbot.rag.modules.preretrieve.RewriteQueryModule;
import com.rubenzu03.rag_chatbot.rag.modules.preretrieve.TranslationQueryModule;
import com.rubenzu03.rag_chatbot.rag.modules.retrieve.DocumentJoinModule;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIService {

    @Value("${VECTOR_DATABASE_FILES_DIR:}")
    private String vectorDatabaseFilesDir;

    @Autowired
    private VectorStore vectordb;

    private final ChatClient chatClient;

    private final QueryTransformerModule queryTransformerModule;
    private final RewriteQueryModule rewriteQueryModule;
    private final TranslationQueryModule translationQueryModule;
    private final QueryExpansionModule queryExpansionModule;
    private final DocumentPostProcessingModule documentPostProcessingModule;
    private final DocumentJoinModule documentJoinModule;


    @Autowired
    public AIService(@Qualifier("llama3ChatClient") ChatClient chatClient,
                     TranslationQueryModule translationQueryModule,
                     RewriteQueryModule rewriteQueryModule, QueryTransformerModule queryTransformerModule, QueryExpansionModule queryExpansionModule,
                     DocumentPostProcessingModule documentPostProcessingModule, DocumentJoinModule documentJoinModule) {
        this.chatClient = chatClient;
        this.translationQueryModule = translationQueryModule;
        this.rewriteQueryModule = rewriteQueryModule;
        this.queryTransformerModule = queryTransformerModule;
        this.queryExpansionModule = queryExpansionModule;
        this.documentPostProcessingModule = documentPostProcessingModule;
        this.documentJoinModule = documentJoinModule;
    }

    public String simpleQueryTest(String query){
        return this.chatClient.prompt(query).call().content();
    }

    public String RAGQueryTest(String query) {
        // Step 1: Pre-retrieval - Query transformation pipeline
        //TODO: Change from String to Query
        Query finalQuery = queryTransformerModule.transformQuery(query);
        finalQuery = rewriteQueryModule.rewriteUserQuery(finalQuery.text());
        finalQuery = translationQueryModule.translateQuery(finalQuery.text());

        // Step 2: Query expansion - Generate multiple query variations
        List<Query> expandedQueries = queryExpansionModule.expandQueries(finalQuery);

        // Add the original final query to expanded queries if not already included
        if (!expandedQueries.contains(finalQuery)) {
            List<Query> allQueries = new ArrayList<>(expandedQueries);
            allQueries.addFirst(finalQuery); // Add original as first query
            expandedQueries = allQueries;
        }

        Map<Query, List<List<Document>>> queryToDocuments = new HashMap<>();

        for (Query expandedQuery : expandedQueries) {
            // Retrieve documents for each expanded query using similarity search
            List<Document> retrievedDocs = vectordb.similaritySearch(
                SearchRequest.builder()
                    .query(expandedQuery.text())
                    .topK(10)  // Retrieve top 10 for each query
                    .build()
            );

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

        // Step 6: Generate RAG response
        if (rankedDocs.isEmpty()) {
            return "I'm sorry, I don't have enough information to answer that question.";
        }

        // Construct context from ranked documents
        String context = rankedDocs.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n"));

        // Generate response using ChatClient with context

        String response = chatClient.prompt()
                .system(ChatClientConfig.DEFAULT_SYSTEM_PROMPT)
                .user(u -> u
                    .text("Context:\n{context}\n\nQuestion: {question}")
                    .param("context", context)
                    .param("question", query))
                .call()
                .content();

        return response;
    }




}
