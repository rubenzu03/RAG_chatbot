package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.application.ports.input.QuestionUseCase;
import com.rubenzu03.rag_chatbot.application.ports.output.QuestionRepositoryPort;
import com.rubenzu03.rag_chatbot.infrastructure.components.RAGContextBuilder;
import com.rubenzu03.rag_chatbot.infrastructure.config.ChatClientConfig;
import com.rubenzu03.rag_chatbot.domain.model.QuestionDTO;
import com.rubenzu03.rag_chatbot.domain.dto.QuestionEvaluationRequest;
import com.rubenzu03.rag_chatbot.domain.dto.QuestionEvaluationResponse;
import com.rubenzu03.rag_chatbot.domain.dto.QuestionResponse;
import com.rubenzu03.rag_chatbot.domain.exception.DocumentsNotFoundException;
import com.rubenzu03.rag_chatbot.domain.exception.QuestionNotFoundException;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.QuestionEntity;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class QuestionModeService implements QuestionUseCase {

    private final RetrievalService retrievalService;
    private final RAGContextBuilder ragContextBuilder;
    private final ChatClient chatClient;
    private final QuestionRepositoryPort questionRepositoryPort;

    public QuestionModeService(@Qualifier("QuestionModeChatClient") ChatClient chatClient,
                               RetrievalService retrievalService,
                               RAGContextBuilder ragContextBuilder,
                               QuestionRepositoryPort generatedQuestionRepository) {
        this.retrievalService = retrievalService;
        this.ragContextBuilder = ragContextBuilder;
        this.chatClient = chatClient;
        this.questionRepositoryPort = generatedQuestionRepository;
    }


    @Override
    public QuestionEvaluationResponse evaluateAnswer(QuestionEvaluationRequest questionEvaluationRequest) {
        QuestionDTO questionDTO = questionRepositoryPort
                .findById(questionEvaluationRequest.getQuestionId())
                .orElseThrow(() -> new QuestionNotFoundException(
                        "Question not found: " + questionEvaluationRequest.getQuestionId()));

        String prompt = String.format(
                ChatClientConfig.EVALUATION_PROMPT,
                questionDTO.getContext(),
                questionDTO.getQuestion(),
                questionEvaluationRequest.getAnswer()
        );

        String fullResponse = chatClient.prompt()
                .user(prompt)
                .call().content();

        return parseEvaluation(fullResponse);
    }

    @Override
    public QuestionResponse generateQuestion() {
        List<Document> docs = retrievalService.retrieveDocuments(new Query("*"), 30);

        if (docs.isEmpty()) {
            throw new DocumentsNotFoundException("No documents found");
        }

        Collections.shuffle(docs);
        List<Document> selectedDocs = docs.subList(0, Math.min(3, docs.size()));

        String context = ragContextBuilder.buildRAGContext(selectedDocs);

        String prompt = String.format(ChatClientConfig.QUESTION_GENERATION_PROMPT, context);

        String question = chatClient.prompt()
                .user(prompt)
                .call().content();


        QuestionDTO questionEntity = new QuestionDTO();
        questionEntity.setQuestion(question);
        questionEntity.setContext(context);

        QuestionEntity saved = questionRepositoryPort.save(questionEntity)
                .orElseThrow(() -> new RuntimeException("Error saving generated question"));

        return new QuestionResponse(saved.getId(), question);
    }

    private QuestionEvaluationResponse parseEvaluation(String fullResponse) {
        String result = "";
        String explanation = fullResponse;

        String lowerCase = fullResponse.toLowerCase();

        if (lowerCase.contains("correcta") && !lowerCase.contains("incorrecta") && !lowerCase.contains("parcial")) {
            result = "CORRECTA";
        } else if (lowerCase.contains("incorrecta")) {
            result = "INCORRECTA";
        } else if (lowerCase.contains("parcial")) {
            result = "PARCIAL";
        }

        int explanationStart = fullResponse.toLowerCase().indexOf("explicación:");
        if (explanationStart >= 0) {
            explanation = fullResponse.substring(explanationStart + "explicación:".length()).trim();
        }

        return new QuestionEvaluationResponse(result, explanation);
    }

    public void deleteAllQuestions() {
        questionRepositoryPort.deleteAll();
    }
}
