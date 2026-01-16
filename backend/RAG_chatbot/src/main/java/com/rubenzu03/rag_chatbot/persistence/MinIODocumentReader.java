package com.rubenzu03.rag_chatbot.persistence;

import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class MinIODocumentReader {

    @Value(value = "${MINIO.ENDPOINT}")
    private String endpoint;

    @Value(value = "${MINIO.ACCESSKEY}")
    private String accessKey;

    @Value(value = "${MINIO.SECRETKEY}")
    private String secretKey;

    private final MinioClient minioClient;


    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey,secretKey)
                .build();
    }


    public MinIODocumentReader(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public List<Document> readAllDocuments(String bucketName){
        List<Document> documents = new ArrayList<>();

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
            throw new RuntimeException(e);
        }
        return documents;
    }
}
