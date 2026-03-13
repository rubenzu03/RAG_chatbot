package com.rubenzu03.rag_chatbot.ragmodules.retrieve;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DocumentJoinModule {

    private final DocumentJoiner documentJoiner;

    public DocumentJoinModule() {
        this.documentJoiner = new ConcatenationDocumentJoiner();
    }

    public List<Document> joinDocuments(Map<Query, List<List<Document>>> queryToDocuments) {
        return documentJoiner.join(queryToDocuments);
    }
}
