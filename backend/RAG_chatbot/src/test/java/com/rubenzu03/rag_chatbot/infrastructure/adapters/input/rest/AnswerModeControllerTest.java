package com.rubenzu03.rag_chatbot.infrastructure.adapters.input.rest;

import com.rubenzu03.rag_chatbot.application.service.AnswerModeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnswerModeControllerTest {

    private AnswerModeService answerModeService;
    private AnswerModeController controller;

    @BeforeEach
    void setUp() {
        answerModeService = mock(AnswerModeService.class);
        controller = new AnswerModeController(answerModeService);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user@example.com", "N/A")
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void askQuery_returnsChatResponse() {
        when(answerModeService.buildConversationKey("user@example.com", "conv")).thenReturn("user@example.com::conv");
        when(answerModeService.answerSimpleQuery("hello", "user@example.com::conv")).thenReturn("answer");

        var response = controller.askQuery("hello", "conv");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResponse()).isEqualTo("answer");
        assertThat(response.getBody().getUserId()).isEqualTo("user@example.com::conv");
        verify(answerModeService).answerSimpleQuery("hello", "user@example.com::conv");
    }

    @Test
    void askQueryUsingRAG_returnsFluxFromService() {
        when(answerModeService.buildConversationKey("user@example.com", null)).thenReturn("user@example.com::default");
        when(answerModeService.AnswerWithRagQuery("hello", "user@example.com::default"))
                .thenReturn(Flux.just("a", "b"));

        var result = controller.askQueryUsingRAG("hello", null).collectList().block();

        assertThat(result).containsExactly("a", "b");
        verify(answerModeService).AnswerWithRagQuery("hello", "user@example.com::default");
    }
}

