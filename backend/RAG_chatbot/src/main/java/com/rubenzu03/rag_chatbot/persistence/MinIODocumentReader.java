package com.rubenzu03.rag_chatbot.persistence;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

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

                try (InputStream stream = minioClient.getObject(
                        GetObjectArgs.builder().bucket(bucketName)
                                .object(item.objectName()).build()
                )) {
                    String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

                    Document doc = new Document(content,
                            Map.of("item_name", item.objectName(),
                                    "bucket", bucketName));

                    documents.add(doc);

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