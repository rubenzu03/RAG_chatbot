package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.infrastructure.components.RAGContextBuilder;
import com.rubenzu03.rag_chatbot.infrastructure.config.ChatClientConfig;
import com.rubenzu03.rag_chatbot.domain.model.Question;
import com.rubenzu03.rag_chatbot.domain.dto.EvaluationRequest;
import com.rubenzu03.rag_chatbot.domain.dto.EvaluationResponse;
import com.rubenzu03.rag_chatbot.domain.dto.QuestionResponse;
import com.rubenzu03.rag_chatbot.domain.exception.DocumentsNotFoundException;
import com.rubenzu03.rag_chatbot.domain.exception.QuestionNotFoundException;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.GeneratedQuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class QuestionModeService {

    private static final Logger log = LoggerFactory.getLogger(QuestionModeService.class);

    private final RetrievalService retrievalService;
    private final RAGContextBuilder ragContextBuilder;
    private final ChatClient chatClient;
    private final GeneratedQuestionRepository generatedQuestionRepository;

    public QuestionModeService(@Qualifier("QuestionModeChatClient") ChatClient chatClient,
                               RetrievalService retrievalService,
                               RAGContextBuilder ragContextBuilder,
                               GeneratedQuestionRepository generatedQuestionRepository) {
        this.retrievalService = retrievalService;
        this.ragContextBuilder = ragContextBuilder;
        this.chatClient = chatClient;
        this.generatedQuestionRepository = generatedQuestionRepository;
    }

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

        Question entity = new Question();
        entity.setQuestion(question);
        entity.setContext(context);
        Question saved = generatedQuestionRepository.save(entity);

        return new QuestionResponse(saved.getId(), question);
    }

    public EvaluationResponse evaluateAnswer(EvaluationRequest evaluationRequest) {
        Question question = generatedQuestionRepository
                .findById(evaluationRequest.getQuestionId())
                .orElseThrow(() -> new QuestionNotFoundException(
                        "Question not found: " + evaluationRequest.getQuestionId()));

        String prompt = String.format(
                ChatClientConfig.EVALUATION_PROMPT,
                question.getContext(),
                question.getQuestion(),
                evaluationRequest.getAnswer()
        );

        String fullResponse = chatClient.prompt()
                .user(prompt)
                .call().content();

        return parseEvaluation(fullResponse);
    }

    private EvaluationResponse parseEvaluation(String fullResponse) {
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

        return new EvaluationResponse(result, explanation);
    }

    public void deleteAllQuestions() {
        generatedQuestionRepository.deleteAll();
    }
}
