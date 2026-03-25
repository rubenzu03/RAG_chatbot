package com.rubenzu03.rag_chatbot.application.service;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnExpression("'${MINIO.ENDPOINT:${MINIO_ENDPOINT:}}' != ''")
public class MinIODocumentReader {

    private static final Logger log = LoggerFactory.getLogger(MinIODocumentReader.class);

    private final MinioClient minioClient;

    @Autowired
    public MinIODocumentReader(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public List<Document> readAllDocuments(String bucketName){
        List<Document> documents = new ArrayList<>();

        log.info("Reading documents from MinIO bucket: {}", bucketName);

        try{
            Iterable<Result<Item>> objects = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).recursive(true).build()
            );


            for (Result<Item> result : objects) {
                Item item = result.get();
                String objectName = item.objectName().toLowerCase();

                try (InputStream stream = minioClient.getObject(
                        GetObjectArgs.builder().bucket(bucketName)
                                .object(item.objectName()).build()
                )) {
                    if (objectName.endsWith(".pdf")) {
                        log.debug("Processing PDF file: {}", item.objectName());
                        byte[] pdfBytes = stream.readAllBytes();
                        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                                .withPagesPerDocument(10)
                                .build();

                        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
                                new InputStreamResource(new ByteArrayInputStream(pdfBytes)),
                                config);

                        List<Document> pdfDocuments = pdfReader.get();
                        for (Document doc : pdfDocuments) {
                            Map<String, Object> metadata = doc.getMetadata();
                            metadata.put("item_name", item.objectName());
                            metadata.put("bucket", bucketName);
                            metadata.put("file_type", "pdf");
                            documents.add(doc);
                        }

                        log.debug("Extracted {} pages from PDF: {}", pdfDocuments.size(), item.objectName());
                    }
                    else if (objectName.endsWith(".txt") || objectName.endsWith(".md")) {
                        log.debug("Processing text file: {}", item.objectName());
                        String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

                        Document doc = new Document(content,
                                Map.of("item_name", item.objectName(),
                                        "bucket", bucketName,
                                        "file_type", "text"));

                        documents.add(doc);
                    }
                    else {
                        log.warn("Unsupported file type for: {}. Skipping.", item.objectName());
                    }
                }
            }
        }
        catch (Exception e){
            log.error("Failed to read documents from MinIO bucket '{}': {}", bucketName, e.getMessage(), e);
            throw new RuntimeException(e);
        }

        log.info("Successfully read {} documents from MinIO bucket '{}'", documents.size(), bucketName);
        return documents;
    }
}


