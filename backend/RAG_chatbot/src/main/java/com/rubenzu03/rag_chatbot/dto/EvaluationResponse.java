package com.rubenzu03.rag_chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResponse {
    //TODO: use enum for results
    private String result;
    private String explanation;
    private String questionId;
    private String sessionId;
}

