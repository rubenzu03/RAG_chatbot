package com.rubenzu03.rag_chatbot.controller;

import com.rubenzu03.rag_chatbot.dto.EvaluationRequest;
import com.rubenzu03.rag_chatbot.dto.EvaluationResponse;
import com.rubenzu03.rag_chatbot.dto.QuestionResponse;
import com.rubenzu03.rag_chatbot.service.QuestionModeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/question-mode")
public class QuestionModeController {

    private final QuestionModeService questionModeService;

    @Autowired
    public QuestionModeController(QuestionModeService questionModeService) {
        this.questionModeService = questionModeService;
    }

    @PostMapping("/generate")
    public ResponseEntity<QuestionResponse> generateQuestion() {
        QuestionResponse questionResponse = questionModeService.generateQuestion();
        return ResponseEntity.ok(questionResponse);
    }

    @PostMapping("/evaluate")
    public ResponseEntity<EvaluationResponse> evaluateAnswer(@RequestBody EvaluationRequest request) {
        EvaluationResponse evaluationResponse = questionModeService.evaluateAnswer(request);
        return ResponseEntity.ok(evaluationResponse);
    }
}
