package com.rubenzu03.rag_chatbot.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class QuestionEvaluationRequest {
    public String questionId;
    public String answer;
}




