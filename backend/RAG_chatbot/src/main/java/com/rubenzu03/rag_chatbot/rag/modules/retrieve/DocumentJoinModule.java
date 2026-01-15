package com.rubenzu03.rag_chatbot.rag.modules.retrieve;

import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.join.DocumentJoiner;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DocumentJoinModule {

    private Map<Query, List<List<Document>>> queryToDocuments;


    public List<Document> joinDocuments(Map<Query, List<List<Document>>> queryToDocuments){
        DocumentJoiner documentJoiner = new ConcatenationDocumentJoiner();
        return documentJoiner.join(queryToDocuments);
    }
}
