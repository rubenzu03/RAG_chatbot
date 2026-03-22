package com.rubenzu03.rag_chatbot.application.ports.output;

import java.util.List;

public interface DocumentRetrievalPort {
    List<String> retrieveDocuments(String query);
}

