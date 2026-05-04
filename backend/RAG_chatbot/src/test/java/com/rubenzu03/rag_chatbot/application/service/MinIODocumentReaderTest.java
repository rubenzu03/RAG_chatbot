package com.rubenzu03.rag_chatbot.application.service;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MinIODocumentReaderTest {

    private MinioClient minioClient;
    private MinIODocumentReader reader;

    @BeforeEach
    void setUp() {
        minioClient = mock(MinioClient.class);
        reader = new MinIODocumentReader(minioClient);
    }

    @Test
    void readAllDocuments_readsTextFiles() throws Exception {
        Result<Item> result = mock(Result.class);
        Item item = mock(Item.class);
        when(item.objectName()).thenReturn("notes.txt");
        when(result.get()).thenReturn(item);
        when(minioClient.listObjects(any(ListObjectsArgs.class))).thenReturn(List.of(result));
        GetObjectResponse objectResponse = mock(GetObjectResponse.class);
        when(objectResponse.readAllBytes()).thenReturn("hello".getBytes(StandardCharsets.UTF_8));
        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenReturn(objectResponse);

        List<Document> documents = reader.readAllDocuments("bucket");

        assertThat(documents).hasSize(1);
        assertThat(documents.getFirst().getMetadata()).containsEntry("file_type", "text");
    }

    @Test
    void readAllDocuments_skipsUnsupportedFiles() throws Exception {
        Result<Item> result = mock(Result.class);
        Item item = mock(Item.class);
        when(item.objectName()).thenReturn("file.bin");
        when(result.get()).thenReturn(item);
        when(minioClient.listObjects(any(ListObjectsArgs.class))).thenReturn(List.of(result));
        GetObjectResponse objectResponse = mock(GetObjectResponse.class);
        when(objectResponse.readAllBytes()).thenReturn("data".getBytes(StandardCharsets.UTF_8));
        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenReturn(objectResponse);

        List<Document> documents = reader.readAllDocuments("bucket");

        assertThat(documents).isEmpty();
    }
}
