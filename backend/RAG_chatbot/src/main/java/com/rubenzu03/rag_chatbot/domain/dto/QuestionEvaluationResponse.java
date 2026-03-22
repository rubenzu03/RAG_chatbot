package com.rubenzu03.rag_chatbot.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionEvaluationResponse {
    private String result;
    private String explanation;
}

