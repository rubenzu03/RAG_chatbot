package com.rubenzu03.rag_chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class EvaluationRequest {

    private String sessionId;
    private String context;
    private String userAnswer;
    private String question;
    private String questionId;

}




