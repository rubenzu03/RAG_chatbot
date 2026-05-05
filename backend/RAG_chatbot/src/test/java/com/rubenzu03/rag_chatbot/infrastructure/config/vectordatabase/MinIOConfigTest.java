package com.rubenzu03.rag_chatbot.infrastructure.config.vectordatabase;

import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class MinIOConfigTest {

    private MinIOConfig config = new MinIOConfig();

    @Test
    void testMinioClientBeanCreation() {
        ReflectionTestUtils.setField(config, "endpoint", "http://localhost:9000");
        ReflectionTestUtils.setField(config, "accessKey", "minioadmin");
        ReflectionTestUtils.setField(config, "secretKey", "minioadmin");

        MinioClient minioClient = config.minioClient();
        assertThat(minioClient).isNotNull();
    }

    @Test
    void testMinioClientWithValidConfig() {
        ReflectionTestUtils.setField(config, "endpoint", "http://127.0.0.1:9000");
        ReflectionTestUtils.setField(config, "accessKey", "admin");
        ReflectionTestUtils.setField(config, "secretKey", "password123");

        MinioClient minioClient = config.minioClient();
        assertThat(minioClient).isNotNull();
    }

    @Test
    void testMinioClientMultipleInstances() {
        ReflectionTestUtils.setField(config, "endpoint", "http://localhost:9000");
        ReflectionTestUtils.setField(config, "accessKey", "key1");
        ReflectionTestUtils.setField(config, "secretKey", "secret1");

        MinioClient client1 = config.minioClient();
        MinioClient client2 = config.minioClient();

        assertThat(client1).isNotNull();
        assertThat(client2).isNotNull();
    }
}
