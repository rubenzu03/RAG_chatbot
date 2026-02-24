package com.rubenzu03.rag_chatbot.controller;

import com.rubenzu03.rag_chatbot.dto.QuestionResponse;
import com.rubenzu03.rag_chatbot.service.QuestionModeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class QuestionModeController {

    private final QuestionModeService questionModeService;

    @Autowired
    public QuestionModeController(QuestionModeService questionModeService) {
        this.questionModeService = questionModeService;
    }

    @PostMapping("/generate")
    public ResponseEntity<QuestionResponse> generateQuestion(){
        QuestionResponse questionResponse = questionModeService.generateQuestion();
        return ResponseEntity.ok(questionResponse);
    }
}
