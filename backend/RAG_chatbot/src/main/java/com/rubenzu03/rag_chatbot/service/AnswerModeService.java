package com.rubenzu03.rag_chatbot.service;

import com.rubenzu03.rag_chatbot.components.RAGContextBuilder;
import com.rubenzu03.rag_chatbot.config.ChatClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnswerModeService {

    private static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";

    private final RetrievalService retrievalService;
    private final RAGContextBuilder ragContextBuilder;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    private static final Logger log = LoggerFactory.getLogger(AnswerModeService.class);


    @Autowired
    public AnswerModeService(@Qualifier("AnswerModeChatClient") ChatClient chatClient,
                             ChatMemory chatMemory, RetrievalService retrievalService, RAGContextBuilder ragContextBuilder) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.retrievalService = retrievalService;
        this.ragContextBuilder = ragContextBuilder;
    }

    public String answerSimpleQuery(String query, String sessionId) {
        return this.chatClient.prompt(query)
                .advisors(advisor -> advisor.param(CHAT_MEMORY_CONVERSATION_ID_KEY, sessionId))
                .call()
                .content();
    }

    public Flux<String> AnswerWithRagQuery(String query, String sessionId) {
        List<Message> historyMessages = chatMemory.get(sessionId);

        chatMemory.add(sessionId, new UserMessage(query));

        List<Document> rankedDocs = retrievalService.retrieveDocuments(new Query(query), sessionId, 10);

        if (rankedDocs.isEmpty()) {
            log.warn("No documents found for query: {}", query);
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

        //TODO: Mover esto a otra parte

        return chatClient.prompt()
                .system(ChatClientConfig.ANSWER_MODE_GENERATION_PROMPT)
                .messages(allMessages)
                .user(promptWithContext)
                .stream()
                .content()
                .doOnNext(token -> {
                    fullResponse.append(token);

                    log.info("[STREAM_TOKEN] session={} token=[{}]", sessionId, token.replace("\n", "\\n"));

                    char[] chars = token.toCharArray();
                    for (int i = 0; i < chars.length; i++) {
                        char c = chars[i];
                        String repr;
                        if (c == '\n') repr = "\\n";
                        else if (c == '\r') repr = "\\r";
                        else if (c == '\t') repr = "\\t";
                        else if (Character.isISOControl(c)) repr = String.format("\\u%04x", (int) c);
                        else repr = Character.toString(c);

                        log.debug("[STREAM_CHAR] session={} tokenIdx={} charIdx={} char='{}' code={}", sessionId, /* token index not tracked */ 0, i, repr, (int) c);
                    }
                })
                .doOnComplete(() -> {
                    chatMemory.add(sessionId, new AssistantMessage(fullResponse.toString()));
                    log.info("Response saved to chat memory for session: {}", sessionId);
                });
    }
}
