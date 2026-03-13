package com.rubenzu03.rag_chatbot.exception;

public class DocumentsNotFoundException extends RuntimeException {
    public DocumentsNotFoundException(String message) {
        super(message);
    }
}
