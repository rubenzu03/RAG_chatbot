package com.rubenzu03.rag_chatbot.service;

import com.rubenzu03.rag_chatbot.components.RAGContextBuilder;
import com.rubenzu03.rag_chatbot.config.ChatClientConfig;
import com.rubenzu03.rag_chatbot.domain.Question;
import com.rubenzu03.rag_chatbot.dto.EvaluationRequest;
import com.rubenzu03.rag_chatbot.dto.EvaluationResponse;
import com.rubenzu03.rag_chatbot.dto.QuestionResponse;
import com.rubenzu03.rag_chatbot.persistence.GeneratedQuestionRepository;
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
            log.error("No documents found");
            return new QuestionResponse(null, "No se encontraron documentos para generar preguntas.");
        }

        Collections.shuffle(docs);
        List<Document> selectedDocs = docs.subList(0, Math.min(3, docs.size()));

        String context = ragContextBuilder.buildRAGContext(selectedDocs);

        String prompt = String.format(ChatClientConfig.QUESTION_GENERATION_PROMPT, context);

        String question = chatClient.prompt()
                .user(prompt)
                .call().content();

        // Persistir la pregunta y su contexto
        Question entity = new Question();
        entity.setQuestion(question);
        entity.setContext(context);
        Question saved = generatedQuestionRepository.save(entity);

        return new QuestionResponse(saved.getId(), question);
    }

    public EvaluationResponse evaluateAnswer(EvaluationRequest evaluationRequest) {
        // Recuperar la pregunta y contexto por ID
        Question question = generatedQuestionRepository
                .findById(evaluationRequest.getQuestionId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pregunta no encontrada con ID: " + evaluationRequest.getQuestionId()));

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
}
