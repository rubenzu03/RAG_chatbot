package com.rubenzu03.rag_chatbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnExpression("'${MINIO.ENDPOINT:${MINIO_ENDPOINT:}}' != ''")
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final MinIODocumentReader minIODocumentReader;
    private final TextSplitter textSplitter;
    private final VectorStore vectorStore;

    @Value("${MINIO.BUCKETNAME:${MINIO_BUCKET_NAME:}}")
    private String bucketName;


    public IngestionService(MinIODocumentReader minIODocumentReader,VectorStore vectorStore) {
        this.minIODocumentReader = minIODocumentReader;
        this.textSplitter = new TokenTextSplitter();
        this.vectorStore = vectorStore;
    }

    public void ingestDocuments(){
        log.info("Starting document ingestion from bucket: {}", bucketName);
        List<Document> documents = minIODocumentReader.readAllDocuments(bucketName);
        if (documents.isEmpty()) {
            log.warn("No documents found in bucket '{}'", bucketName);
            return;
        }
        log.info("Splitting {} documents into chunks", documents.size());
        List<Document> splitDocuments = textSplitter.split(documents);
        log.info("Adding {} document chunks to vector store", splitDocuments.size());
        vectorStore.add(splitDocuments);
        log.info("Document ingestion completed successfully");
    }

}