package com.rubenzu03.rag_chatbot.infrastructure.ragmodules.postretrieve;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentPostProcessingModuleTest {

    @Test
    void rankAndFilterDocuments_deduplicatesByParentAndChunk() {
        DocumentPostProcessingModule module = new DocumentPostProcessingModule();

        Document doc1 = new Document("a", Map.of("parent_document_id", "p1", "chunk_index", 1));
        Document doc2 = new Document("b", Map.of("parent_document_id", "p1", "chunk_index", 1));
        Document doc3 = new Document("c", Map.of("parent_document_id", "p2"));
        Document doc4 = new Document("d");

        List<Document> result = module.rankAndFilterDocuments(List.of(doc1, doc2, doc3, doc4), 0.0, 10);

        assertThat(result).hasSize(3);
    }
}

