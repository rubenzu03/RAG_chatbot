package com.rubenzu03.rag_chatbot.service;

import com.rubenzu03.rag_chatbot.persistence.MinIODocumentReader;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngestionService {

    private final MinIODocumentReader minIODocumentReader;
    private final TextSplitter textSplitter;
    private final VectorStore vectorStore;

    @Value(value = "${MINIO.BUCKETNAME}")
    private String bucketName;


    public IngestionService(MinIODocumentReader minIODocumentReader,VectorStore vectorStore) {
        this.minIODocumentReader = minIODocumentReader;
        this.textSplitter = new TokenTextSplitter();
        this.vectorStore = vectorStore;
    }

    public void ingestDocuments(){
        List<Document> documents = minIODocumentReader.readAllDocuments(bucketName);
        List<Document> splitDocuments = textSplitter.split(documents);
        vectorStore.add(splitDocuments);
    }

}
