package com.rubenzu03.rag_chatbot.infrastructure.ragmodules.preretrieve;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class QueryTransformerModule {

    private static final Logger log = LoggerFactory.getLogger(QueryTransformerModule.class);

    private final ChatMemory chatMemory;
    private final QueryTransformer queryTransformer;

    private static final PromptTemplate COMPRESSION_PROMPT = new PromptTemplate("""
            Given the conversation history and the user's current query, rephrase the query so that it is self-contained and clear, including any relevant context from the history.
            Focus on topics of programming, software engineering and software architecture. If the query refers to something mentioned earlier, include it explicitly.
            Conversation History: {history}
            
            Current Query: {query}
            Rephrased Query:"
    Polished (natural) English version (placeholders preserved): "Using the conversation history and the user's current query, rewrite the query so it is self-contained and clear, incorporating any relevant context from the history.
    Focus on programming, software engineering, and software architecture. If the query refers to something mentioned earlier, make that reference explicit.
    Conversation history: {history}
    Current user query: {query}
    Rewritten query:
    """);

    @Autowired
    public QueryTransformerModule(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
        this.queryTransformer = CompressionQueryTransformer.builder()
                .promptTemplate(COMPRESSION_PROMPT)
                .chatClientBuilder(chatClientBuilder)
                .build();
    }

    public Query transformQuery(String rawQuery, String userId) {
        List<Message> historyMessages = getConversationHistory(userId);

        log.info("=== QueryTransformer Debug ===");
        log.info("User ID: {}", userId);
        log.info("Raw Query: {}", rawQuery);
        log.info("History messages count: {}", historyMessages.size());

        if (!historyMessages.isEmpty()) {
            log.info("History messages:");
            for (int i = 0; i < historyMessages.size(); i++) {
                Message msg = historyMessages.get(i);
                log.info("  [{}] Type: {}, Content: {}", i, msg.getMessageType(), msg.getText());
            }
        } else {
            log.info("No history messages available");
        }

        Query query = Query.builder()
                .text(rawQuery)
                .history(historyMessages)
                .build();

        log.info("Query object created - Text: {}, History size: {}", query.text(), query.history().size());

        Query transformedQuery = queryTransformer.transform(query);

        log.info("Transformed query: {}", transformedQuery.text());
        log.info("=== End QueryTransformer Debug ===");

        return transformedQuery;
    }

    private List<Message> getConversationHistory(String userId) {
        if (userId == null || userId.isBlank()) {
            return Collections.emptyList();
        }
        return chatMemory.get(userId);
    }
}
