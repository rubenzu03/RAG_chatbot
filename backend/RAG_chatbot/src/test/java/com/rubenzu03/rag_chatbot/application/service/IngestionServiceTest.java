package com.rubenzu03.rag_chatbot.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IngestionServiceTest {

    private MinIODocumentReader minIODocumentReader;
    private VectorStore vectorStore;
    private IngestionService service;

    @BeforeEach
    void setUp() {
        minIODocumentReader = mock(MinIODocumentReader.class);
        vectorStore = mock(VectorStore.class);
        service = new IngestionService(minIODocumentReader, vectorStore);
        ReflectionTestUtils.setField(service, "bucketName", "bucket");
        ReflectionTestUtils.setField(service, "ingestionBatchSize", 1);
        TextSplitter textSplitter = mock(TextSplitter.class);
        ReflectionTestUtils.setField(service, "textSplitter", textSplitter);
        when(textSplitter.split(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void ingestDocuments_noDocuments_doesNothing() {
        when(minIODocumentReader.readAllDocuments("bucket")).thenReturn(List.of());

        service.ingestDocuments();

        verify(vectorStore, never()).add(anyList());
    }

    @Test
    void ingestDocuments_addsBatches() {
        when(minIODocumentReader.readAllDocuments("bucket"))
                .thenReturn(List.of(new Document("a"), new Document("b")));

        service.ingestDocuments();

        verify(vectorStore).add(anyList());
    }
}
