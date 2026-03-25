package com.rubenzu03.rag_chatbot.application.service;

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

    @Value("${ingestion.batch-size:500}")
    private int ingestionBatchSize;

    public IngestionService(MinIODocumentReader minIODocumentReader, VectorStore vectorStore) {
        this.minIODocumentReader = minIODocumentReader;
        this.textSplitter = new TokenTextSplitter(true);
        this.vectorStore = vectorStore;
    }

    public void ingestDocuments() {
        //TODO: Check if documents have been already ingested
        log.info("Starting document ingestion from bucket: {}", bucketName);
        List<Document> documents = minIODocumentReader.readAllDocuments(bucketName);
        if (documents.isEmpty()) {
            log.warn("No documents found in bucket '{}'", bucketName);
            return;
        }
        log.info("Splitting {} documents into chunks", documents.size());
        List<Document> splitDocuments = textSplitter.split(documents);
        log.info("Adding {} document chunks to vector store in batches of {}",
                splitDocuments.size(), ingestionBatchSize);

        long startTime = System.currentTimeMillis();
        addDocumentsInBatches(splitDocuments);
        long duration = (System.currentTimeMillis() - startTime) / 1000;

        log.info("Document ingestion completed successfully in {} seconds", duration);
    }

    private void addDocumentsInBatches(List<Document> documents) {
        int totalDocuments = documents.size();
        int totalBatches = (int) Math.ceil((double) totalDocuments / ingestionBatchSize);

        for (int i = 0; i < totalDocuments; i += ingestionBatchSize) {
            int end = Math.min(i + ingestionBatchSize, totalDocuments);
            List<Document> batch = documents.subList(i, end);
            int currentBatch = (i / ingestionBatchSize) + 1;

            log.info("Processing batch {}/{} ({} documents, {}% complete)",
                    currentBatch, totalBatches, batch.size(),
                    Math.round((double) end / totalDocuments * 100));

            long batchStart = System.currentTimeMillis();
            vectorStore.add(batch);
            long batchDuration = System.currentTimeMillis() - batchStart;

            log.debug("Batch {} completed in {} ms", currentBatch, batchDuration);
        }
    }
}