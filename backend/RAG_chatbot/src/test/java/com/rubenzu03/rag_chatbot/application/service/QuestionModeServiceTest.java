package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.application.ports.output.QuestionRepositoryPort;
import com.rubenzu03.rag_chatbot.domain.dto.QuestionEvaluationRequest;
import com.rubenzu03.rag_chatbot.domain.dto.QuestionEvaluationResponse;
import com.rubenzu03.rag_chatbot.domain.dto.QuestionResponse;
import com.rubenzu03.rag_chatbot.domain.exception.DocumentsNotFoundException;
import com.rubenzu03.rag_chatbot.domain.exception.QuestionNotFoundException;
import com.rubenzu03.rag_chatbot.domain.model.QuestionDTO;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.QuestionEntity;
import com.rubenzu03.rag_chatbot.infrastructure.components.RAGContextBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuestionModeServiceTest {

    private QuestionModeService service;

    @Mock
    private RetrievalService retrievalService;
    @Mock
    private RAGContextBuilder ragContextBuilder;
    @Mock
    private QuestionRepositoryPort questionRepositoryPort;

    private ChatClient chatClient;
    private ChatClient evaluationChatClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        evaluationChatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        service = new QuestionModeService(chatClient, evaluationChatClient, retrievalService, ragContextBuilder, questionRepositoryPort);
    }

    @Test
    void generateQuestion_success() {
        List<Document> docs = new java.util.ArrayList<>(List.of(new Document("doc1"), new Document("doc2")));
        when(retrievalService.retrieveDocuments(any(), anyInt())).thenReturn(docs);
        when(ragContextBuilder.buildRAGContext(docs)).thenReturn("ctx");
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("What is RAG?");

        QuestionEntity saved = new QuestionEntity();
        saved.setId("q-1");
        saved.setQuestion("What is RAG?");
        saved.setContext("ctx");
        when(questionRepositoryPort.save(any(QuestionDTO.class))).thenReturn(Optional.of(saved));

        QuestionResponse response = service.generateQuestion();

        assertThat(response.getQuestionId()).isEqualTo("q-1");
        assertThat(response.getQuestion()).isEqualTo("What is RAG?");
        verify(questionRepositoryPort).save(any(QuestionDTO.class));
    }

    @Test
    void generateQuestion_noDocs_throws() {
        when(retrievalService.retrieveDocuments(any(), anyInt())).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> service.generateQuestion())
                .isInstanceOf(DocumentsNotFoundException.class)
                .hasMessage("No documents found");
    }

    @Test
    void evaluateAnswer_success() {
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setContext("ctx");
        questionDTO.setQuestion("q?");
        when(questionRepositoryPort.findById("q-1")).thenReturn(Optional.of(questionDTO));
        when(evaluationChatClient.prompt().user(anyString()).call().content())
                .thenReturn("{ \"result\": \"CORRECT\", \"explanation\": \"ok\" }");

        QuestionEvaluationResponse response = service.evaluateAnswer(new QuestionEvaluationRequest("q-1", "answer"));

        assertThat(response.getResult()).isEqualTo("CORRECT");
        assertThat(response.getExplanation()).isEqualTo("ok");
    }

    @Test
    void evaluateAnswer_retryFails_returnsIncorrect() {
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setContext("ctx");
        questionDTO.setQuestion("q?");
        when(questionRepositoryPort.findById("q-2")).thenReturn(Optional.of(questionDTO));
        when(evaluationChatClient.prompt().user(anyString()).call().content())
                .thenReturn("BAD", "STILL_BAD");

        QuestionEvaluationResponse response = service.evaluateAnswer(new QuestionEvaluationRequest("q-2", "answer"));

        assertThat(response.getResult()).isEqualTo("INCORRECT");
        assertThat(response.getExplanation()).contains("required JSON format after retry");
    }

    @Test
    void evaluateAnswer_missingQuestion_throws() {
        when(questionRepositoryPort.findById("q-missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.evaluateAnswer(new QuestionEvaluationRequest("q-missing", "a")))
                .isInstanceOf(QuestionNotFoundException.class)
                .hasMessageContaining("Question not found");
    }
}
