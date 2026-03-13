package com.rubenzu03.rag_chatbot.service;

import com.rubenzu03.rag_chatbot.components.RAGContextBuilder;
import com.rubenzu03.rag_chatbot.config.ChatClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
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

@Service
public class AnswerModeService {

    private static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    private final int TOP_K_SEARCH = 10;

    private final RetrievalService retrievalService;
    private final RAGContextBuilder ragContextBuilder;
    private final ChatClient chatClient;
    private final ChatHistoryService chatHistoryService;
    private final TransformQueryService transformQueryService;

    private static final Logger log = LoggerFactory.getLogger(AnswerModeService.class);


    @Autowired
    public AnswerModeService(@Qualifier("AnswerModeChatClient") ChatClient chatClient,
                             ChatHistoryService chatHistoryService, RetrievalService retrievalService,
                             RAGContextBuilder ragContextBuilder, TransformQueryService transformQueryService) {
        this.chatClient = chatClient;
        this.chatHistoryService = chatHistoryService;
        this.retrievalService = retrievalService;
        this.ragContextBuilder = ragContextBuilder;
        this.transformQueryService = transformQueryService;
    }

    public String answerSimpleQuery(String query, String userId) {
        return this.chatClient.prompt(query)
                .advisors(advisor -> advisor.param(CHAT_MEMORY_CONVERSATION_ID_KEY, userId))
                .call()
                .content();
    }

    public Flux<String> AnswerWithRagQuery(String query, String userId) {
        List<Message> historyMessages = chatHistoryService.getChatHistory(userId);

        chatHistoryService.addUserMessage(userId, new UserMessage(query));

        Query transformedQuery = transformQueryService.transformQuery(new Query(query), userId);
        List<Document> rankedDocs = retrievalService.retrieveDocuments(transformedQuery, TOP_K_SEARCH);

        if (rankedDocs.isEmpty()) {
            log.warn("No documents found for query: {}", transformedQuery.text());
        }

        String context = ragContextBuilder.buildRAGContext(rankedDocs);

        log.info("Response generated with {} documents", rankedDocs.size());

        List<Message> allMessages = new ArrayList<>(historyMessages);

        String promptWithContext = String.format(
                "Context:\n%s\n\nQuestion: %s",
                context,
                query
        );
        StringBuilder fullResponse = new StringBuilder();

        return chatClient.prompt()
                .system(ChatClientConfig.ANSWER_MODE_GENERATION_PROMPT)
                .messages(allMessages)
                .user(promptWithContext)
                .stream()
                .content()
                .doOnNext(token -> {
                    fullResponse.append(token);
                    debugStream(token, userId);
                })
                .doOnComplete(() -> {
                    chatHistoryService.addAssistantMessage(userId, new AssistantMessage(fullResponse.toString()));
                    log.info("Response saved to chat memory for user: {}", userId);
                });
    }

    private void debugStream(String token, String userId) {
        log.info("[STREAM_TOKEN] user={} token=[{}]", userId, token.replace("\n", "\\n"));

        char[] chars = token.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            String repr;
            if (c == '\n') repr = "\\n";
            else if (c == '\r') repr = "\\r";
            else if (c == '\t') repr = "\\t";
            else if (Character.isISOControl(c)) repr = String.format("\\u%04x", (int) c);
            else repr = Character.toString(c);

            log.debug("[STREAM_CHAR] user={} tokenIdx={} charIdx={} char='{}' code={}", userId, 0, i, repr, (int) c);
        }
    }
}
