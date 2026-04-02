package com.rubenzu03.rag_chatbot.infrastructure.config.vectordatabase;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinIOConfig {

    @Value("${MINIO.ENDPOINT:${MINIO_ENDPOINT:}}")
    private String endpoint;

    @Value("${MINIO.ACCESSKEY:${MINIO_ACCESS_KEY:}}")
    private String accessKey;

    @Value("${MINIO.SECRETKEY:${MINIO_SECRET_KEY:}}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}