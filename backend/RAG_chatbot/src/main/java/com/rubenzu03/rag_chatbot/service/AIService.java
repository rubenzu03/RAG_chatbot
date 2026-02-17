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
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
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
    private final ChatMemory chatMemory;

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
                     ChatMemory chatMemory,
                     TranslationQueryModule translationQueryModule,
                     RewriteQueryModule rewriteQueryModule, QueryTransformerModule queryTransformerModule, QueryExpansionModule queryExpansionModule,
                     DocumentSearchModule documentSearchModule,
                     DocumentPostProcessingModule documentPostProcessingModule, DocumentJoinModule documentJoinModule) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.translationQueryModule = translationQueryModule;
        this.rewriteQueryModule = rewriteQueryModule;
        this.queryTransformerModule = queryTransformerModule;
        this.queryExpansionModule = queryExpansionModule;
        this.documentSearchModule = documentSearchModule;
        this.documentPostProcessingModule = documentPostProcessingModule;
        this.documentJoinModule = documentJoinModule;
    }

    public String simpleQueryTest(String query, String sessionId) {
        return this.chatClient.prompt(query)
                .advisors(advisor -> advisor.param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId))
                .call()
                .content();
    }

    public Flux<String> RAGQueryTest(String query, String sessionId) {
        List<Message> historyMessages = chatMemory.get(sessionId);

        chatMemory.add(sessionId, new UserMessage(query));

        Query finalQuery = queryTransformerModule.transformQuery(query, sessionId);
        finalQuery = rewriteQueryModule.rewriteUserQuery(finalQuery.text());
        finalQuery = translationQueryModule.translateQuery(finalQuery.text());

        List<Query> expandedQueries = queryExpansionModule.expandQueries(finalQuery);
        if (!expandedQueries.contains(finalQuery)) {
            List<Query> allQueries = new ArrayList<>(expandedQueries);
            allQueries.addFirst(finalQuery);
            expandedQueries = allQueries;
        }

        Map<Query, List<List<Document>>> queryToDocuments = new HashMap<>();
        for (Query expandedQuery : expandedQueries) {
            List<Document> retrievedDocs = documentSearchModule.retrieveDocuments(expandedQuery, 20, 0.3);
            queryToDocuments.put(expandedQuery, List.of(retrievedDocs));
        }

        List<Document> joinedDocs = documentJoinModule.joinDocuments(queryToDocuments);

        List<Document> rankedDocs = documentPostProcessingModule.rankAndFilterDocuments(
                joinedDocs,
                0.4,
                10
        );

        if (rankedDocs.isEmpty()) {
            log.warn("No documents found for query: {}", query);
        }

        String context = rankedDocs.stream()
                .map(doc -> {
                    String formatted = doc.getFormattedContent();
                    int contentStart = formatted.indexOf('\n');
                    return contentStart > 0 ? formatted.substring(contentStart + 1).trim() : formatted;
                })
                .collect(Collectors.joining("\n\n"));

        log.info("Response generated with {} documents", rankedDocs.size());

        List<Message> allMessages = new ArrayList<>(historyMessages);

        String promptWithContext = String.format(
                "Context:\n%s\n\nQuestion: %s",
                context,
                query
        );
        StringBuilder fullResponse = new StringBuilder();

        return chatClient.prompt()
                .system(ChatClientConfig.DEFAULT_SYSTEM_PROMPT)
                .messages(allMessages)
                .user(promptWithContext)
                .stream()
                .content()
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    chatMemory.add(sessionId, new AssistantMessage(fullResponse.toString()));
                    log.info("Response saved to chat memory for session: {}", sessionId);
                });
    }
}
