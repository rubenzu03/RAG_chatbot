package com.rubenzu03.rag_chatbot.infrastructure.adapters.input.rest;

import com.rubenzu03.rag_chatbot.application.ports.input.QuestionUseCase;
import com.rubenzu03.rag_chatbot.domain.dto.QuestionEvaluationRequest;
import com.rubenzu03.rag_chatbot.domain.dto.QuestionEvaluationResponse;
import com.rubenzu03.rag_chatbot.domain.dto.QuestionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/question-mode")
public class QuestionModeController {

    private final QuestionUseCase questionUseCase;

    @Autowired
    public QuestionModeController(QuestionUseCase questionUseCase) {
        this.questionUseCase = questionUseCase;
    }

    @PostMapping("/generate")
    public ResponseEntity<QuestionResponse> generateQuestion() {
        QuestionResponse questionResponse = questionUseCase.generateQuestion();
        return ResponseEntity.ok(questionResponse);
    }

    @PostMapping("/evaluate")
    public ResponseEntity<QuestionEvaluationResponse> evaluateAnswer(@RequestBody QuestionEvaluationRequest request) {
        QuestionEvaluationResponse questionEvaluationResponse = questionUseCase.evaluateAnswer(request);
        return ResponseEntity.ok(questionEvaluationResponse);
    }
}
