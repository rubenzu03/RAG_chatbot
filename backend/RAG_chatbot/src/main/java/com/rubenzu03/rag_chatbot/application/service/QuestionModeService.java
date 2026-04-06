package com.rubenzu03.rag_chatbot.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.text.Normalizer;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class QuestionModeService implements QuestionUseCase {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionModeService.class);

    private final RetrievalService retrievalService;
    private final RAGContextBuilder ragContextBuilder;
    private final ChatClient chatClient;
    private final ChatClient evaluationChatClient;
    private final QuestionRepositoryPort questionRepositoryPort;

    public QuestionModeService(@Qualifier("QuestionModeChatClient") ChatClient chatClient,
                               @Qualifier("EvaluationChatClient") ChatClient evaluationChatClient,
                               RetrievalService retrievalService,
                               RAGContextBuilder ragContextBuilder,
                               QuestionRepositoryPort generatedQuestionRepository) {
        this.retrievalService = retrievalService;
        this.ragContextBuilder = ragContextBuilder;
        this.chatClient = chatClient;
        this.evaluationChatClient = evaluationChatClient;
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

        String fullResponse = evaluationChatClient.prompt()
                .user(prompt)
                .call().content();

        if (fullResponse == null) fullResponse = "";

        String sanitized = sanitizeJson(fullResponse);
        try {
            OBJECT_MAPPER.readTree(sanitized);
            return parseEvaluation(sanitized);
        } catch (Exception firstParseException) {
            String repairPrompt = "The previous response was not valid JSON. REFORMAT only the previous output into the required JSON object with keys 'result' and 'explanation' and NOTHING ELSE. Previous output:\n" + fullResponse;
            String repaired = evaluationChatClient.prompt()
                    .user(repairPrompt)
                    .call().content();

            if (repaired == null) repaired = "";

            String repairedSanitized = sanitizeJson(repaired);
            try {
                OBJECT_MAPPER.readTree(repairedSanitized);
                return parseEvaluation(repairedSanitized);
            } catch (Exception secondParseException) {
                LOGGER.warn("Evaluator JSON parse failed. original='{}' sanitized='{}' repaired='{}' repairedSanitized='{}'",
                        fullResponse, sanitized, repaired, repairedSanitized);
                return new QuestionEvaluationResponse("INCORRECT", "Evaluator did not return the required JSON format after retry.");
            }
        }
    }

    @Override
    public QuestionResponse generateQuestion() {
        List<Document> docs = retrievalService.retrieveDocuments(new Query("*"), 200);

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
        if (fullResponse == null || fullResponse.isBlank()) {
            return new QuestionEvaluationResponse("INCORRECT", "The model returned an empty evaluation.");
        }

        String result = null;
        String explanation = null;

        try {
            JsonNode root = OBJECT_MAPPER.readTree(fullResponse);
            if (root != null && root.isObject()) {
                result = normalizeResult(root.path("result").asText(null));
                explanation = root.path("explanation").asText(null);
            }
        } catch (Exception e) {
            return new QuestionEvaluationResponse("INCORRECT", "Evaluator did not return the required JSON format.");
        }

        if (result == null) {
            // If result could not be normalized from the JSON, treat as INCORRECT to avoid leniency.
            return new QuestionEvaluationResponse("INCORRECT", "Evaluator returned invalid or missing 'result' field.");
        }

        if (explanation == null || explanation.isBlank()) {
            explanation = "No explanation provided by evaluator.";
        }

        if (explanation.isBlank()) {
            explanation = "The answer could not be evaluated with the expected format.";
        }

        return new QuestionEvaluationResponse(result, explanation.trim());
    }

    private String normalizeResult(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .trim();

        if (normalized.contains("INCORRECT") || normalized.contains("INCORRECTA") || normalized.contains("INCORRECTO")) {
            return "INCORRECT";
        }
        if (normalized.contains("PARTIAL") || normalized.contains("PARCIAL")) {
            return "PARTIAL";
        }
        if (normalized.contains("CORRECT") || normalized.contains("CORRECTA") || normalized.contains("CORRECTO")) {
            return "CORRECT";
        }
        return null;
    }

    private String extractLegacyExplanation(String fullResponse) {
        String lower = fullResponse.toLowerCase(Locale.ROOT);
        int explanationStart = lower.indexOf("explanation:");

        if (explanationStart >= 0) {
            return fullResponse.substring(explanationStart + "explanation:".length()).trim();
        }

        explanationStart = lower.indexOf("explicacion:");
        if (explanationStart >= 0) {
            return fullResponse.substring(explanationStart + "explicacion:".length()).trim();
        }

        explanationStart = lower.indexOf("explicación:");
        if (explanationStart >= 0) {
            return fullResponse.substring(explanationStart + "explicación:".length()).trim();
        }

        return fullResponse.trim();
    }

    /**
     * Try to clean typical LLM formatting around JSON outputs so we can parse them reliably.
     * - remove markdown code fences (``` or ```json)
     * - extract first {...} block
     * - replace smart quotes with straight quotes
     */
    private String sanitizeJson(String s) {
        if (s == null) return "";
        String trimmed = s.trim();

        // If the model returned a fenced code block like ```json\n{...}\n```,
        // extract the content inside the first pair of triple backticks instead of removing it.
        int fenceStart = trimmed.indexOf("```");
        if (fenceStart >= 0) {
            int fenceEnd = trimmed.indexOf("```", fenceStart + 3);
            if (fenceEnd > fenceStart) {
                String inside = trimmed.substring(fenceStart + 3, fenceEnd).trim();
                // If the inside starts with a language token like "json", remove that first line.
                int firstNewline = inside.indexOf('\n');
                if (firstNewline > 0) {
                    String firstLine = inside.substring(0, firstNewline).trim();
                    if (firstLine.matches("^[A-Za-z0-9_-]+$")) {
                        inside = inside.substring(firstNewline + 1).trim();
                    }
                }
                trimmed = inside;
            }
        }

        // Remove any remaining single backticks
        trimmed = trimmed.replace("`", "");

        // Replace Unicode smart quotes with ASCII equivalents
        trimmed = trimmed.replace('‘', '\'')
                .replace('’', '\'')
                .replace('“', '"')
                .replace('”', '"');

        // Try to extract the first JSON object block if present
        int first = trimmed.indexOf('{');
        int last = trimmed.lastIndexOf('}');
        if (first >= 0 && last > first) {
            return trimmed.substring(first, last + 1).trim();
        }

        return trimmed;
    }

    public void deleteAllQuestions() {
        questionRepositoryPort.deleteAll();
    }
}
