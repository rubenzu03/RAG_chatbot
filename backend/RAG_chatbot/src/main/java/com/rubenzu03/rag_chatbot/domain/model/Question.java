package com.rubenzu03.rag_chatbot.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
public class Question {

    private String id;
    private String question;
    private String context;
    private Timestamp createdAt;

    public Question(String id, String question, String context, Timestamp createdAt) {
        this.id = id;
        this.question = question;
        this.context = context;
        this.createdAt = createdAt;
    }
}


