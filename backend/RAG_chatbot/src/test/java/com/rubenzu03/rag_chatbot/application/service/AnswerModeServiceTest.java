package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.infrastructure.components.RAGContextBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnswerModeServiceTest {

    private AnswerModeService service;
    private RetrievalService retrievalService;
    private RAGContextBuilder ragContextBuilder;
    private TransformQueryService transformQueryService;
    private ChatClient chatClient;

    @BeforeEach
    void setUp() {
        retrievalService = mock(RetrievalService.class);
        ragContextBuilder = mock(RAGContextBuilder.class);
        transformQueryService = mock(TransformQueryService.class);
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        service = new AnswerModeService(chatClient, retrievalService, ragContextBuilder, transformQueryService);
    }

    @Test
    void answerSimpleQuery_returnsContent() {
        when(chatClient.prompt(anyString())
                .advisors(anyAdvisorConsumer())
                .call().content()).thenReturn("answer");

        String response = service.answerSimpleQuery("hola", "conv-1");

        assertThat(response).isEqualTo("answer");
    }

    @Test
    void answerWithRagQuery_streamsTokens() {
        Query transformed = new Query("q");
        when(transformQueryService.transformQuery(any(Query.class), anyString())).thenReturn(transformed);
        when(retrievalService.retrieveDocuments(transformed, 10)).thenReturn(List.of(new Document("doc")));
        when(ragContextBuilder.buildRAGContext(any())).thenReturn("ctx");
        when(chatClient.prompt().system(anyString())
                .advisors(anyAdvisorConsumer())
                .user(anyString()).stream().content())
                .thenReturn(Flux.just("a", "b"));

        List<String> tokens = service.AnswerWithRagQuery("q", "conv").collectList().block();

        assertThat(tokens).containsExactly("a", "b");
    }

    @Test
    void buildConversationKey_defaultsWhenNulls() {
        String key = service.buildConversationKey(null, "");

        assertThat(key).isEqualTo("anonymous::default");
    }

    @Test
    void answerWithRagQuery_emptyDocs_stillStreams() {
        Query transformed = new Query("q");
        when(transformQueryService.transformQuery(any(Query.class), anyString())).thenReturn(transformed);
        when(retrievalService.retrieveDocuments(transformed, 10)).thenReturn(Collections.emptyList());
        when(ragContextBuilder.buildRAGContext(any())).thenReturn("");
        when(chatClient.prompt().system(anyString())
                .advisors(anyAdvisorConsumer())
                .user(anyString()).stream().content())
                .thenReturn(Flux.just("x"));

        List<String> tokens = service.AnswerWithRagQuery("q", "conv").collectList().block();

        assertThat(tokens).containsExactly("x");
    }

    private Consumer<ChatClient.AdvisorSpec> anyAdvisorConsumer() {
        return org.mockito.ArgumentMatchers.any();
    }
}
