package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.infrastructure.components.RAGContextBuilder;
import com.rubenzu03.rag_chatbot.infrastructure.config.ChatClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class AnswerModeService {

    private static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    private static final String DEFAULT_CONVERSATION_ID = "default";
    private final int TOP_K_SEARCH = 10;

    private final RetrievalService retrievalService;
    private final RAGContextBuilder ragContextBuilder;
    private final ChatClient chatClient;
    private final TransformQueryService transformQueryService;

    private static final Logger log = LoggerFactory.getLogger(AnswerModeService.class);


    @Autowired
    public AnswerModeService(@Qualifier("AnswerModeChatClient") ChatClient chatClient,
                             RetrievalService retrievalService,
                             RAGContextBuilder ragContextBuilder, TransformQueryService transformQueryService) {
        this.chatClient = chatClient;
        this.retrievalService = retrievalService;
        this.ragContextBuilder = ragContextBuilder;
        this.transformQueryService = transformQueryService;
    }

    public String answerSimpleQuery(String query, String conversationKey) {
        log.info("answerSimpleQuery() conversationKey={}", conversationKey);
        return this.chatClient.prompt(query)
                .advisors(advisor -> {
                    advisor.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationKey);
                    advisor.param("conversationId", conversationKey);
                    advisor.param("conversation_id", conversationKey);
                })
                .call()
                .content();
    }

    public Flux<String> AnswerWithRagQuery(String query, String conversationKey) {
        Query transformedQuery = transformQueryService.transformQuery(new Query(query), conversationKey);
        List<Document> rankedDocs = retrievalService.retrieveDocuments(transformedQuery, TOP_K_SEARCH);

        if (rankedDocs.isEmpty()) {
            log.warn("No documents found for query: {}", transformedQuery.text());
        }

        String context = ragContextBuilder.buildRAGContext(rankedDocs);

        log.info("Response generated with {} documents", rankedDocs.size());

        String promptWithContext = String.format(
                "Context:\n%s\n\nQuestion: %s",
                context,
                query
        );

        log.info("AnswerWithRagQuery() conversationKey={} docs={}", conversationKey, rankedDocs.size());
        return chatClient.prompt()
            .system(ChatClientConfig.ANSWER_MODE_GENERATION_PROMPT)
            .advisors(advisor -> {
                advisor.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationKey);
                advisor.param("conversationId", conversationKey);
                advisor.param("conversation_id", conversationKey);
            })
            .user(promptWithContext)
            .stream()
            .content()
            .doOnNext(token -> debugStream(token, conversationKey));
    }

    public String buildConversationKey(String userId, String conversationId) {
        String safeUserId = (userId == null || userId.isBlank()) ? "anonymous" : userId.trim();
        String safeConversationId = (conversationId == null || conversationId.isBlank())
                ? DEFAULT_CONVERSATION_ID
                : conversationId.trim();
        return safeUserId + "::" + safeConversationId;
    }

    private void debugStream(String token, String conversationKey) {
        log.info("[STREAM_TOKEN] conversation={} token=[{}]", conversationKey, token.replace("\n", "\\n"));

        char[] chars = token.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            String repr;
            if (c == '\n') repr = "\\n";
            else if (c == '\r') repr = "\\r";
            else if (c == '\t') repr = "\\t";
            else if (Character.isISOControl(c)) repr = String.format("\\u%04x", (int) c);
            else repr = Character.toString(c);

            log.debug("[STREAM_CHAR] conversation={} tokenIdx={} charIdx={} char='{}' code={}", conversationKey, 0, i, repr, (int) c);
        }
    }
}
