package com.rubenzu03.rag_chatbot.domain.exception;

public class DocumentsNotFoundException extends RuntimeException {
    public DocumentsNotFoundException(String message) {
        super(message);
    }
}
