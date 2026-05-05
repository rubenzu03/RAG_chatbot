package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.infrastructure.ragmodules.postretrieve.DocumentPostProcessingModule;
import com.rubenzu03.rag_chatbot.infrastructure.ragmodules.preretrieve.QueryExpansionModule;
import com.rubenzu03.rag_chatbot.infrastructure.ragmodules.preretrieve.TranslationQueryModule;
import com.rubenzu03.rag_chatbot.infrastructure.ragmodules.retrieve.DocumentJoinModule;
import com.rubenzu03.rag_chatbot.infrastructure.ragmodules.retrieve.DocumentSearchModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RetrievalServiceTest {

    private RetrievalService service;
    private TranslationQueryModule translationQueryModule;
    private QueryExpansionModule queryExpansionModule;
    private DocumentSearchModule documentSearchModule;
    private DocumentJoinModule documentJoinModule;
    private DocumentPostProcessingModule documentPostProcessingModule;

    @BeforeEach
    void setUp() {
        translationQueryModule = mock(TranslationQueryModule.class);
        queryExpansionModule = mock(QueryExpansionModule.class);
        documentSearchModule = mock(DocumentSearchModule.class);
        documentJoinModule = mock(DocumentJoinModule.class);
        documentPostProcessingModule = mock(DocumentPostProcessingModule.class);
        service = new RetrievalService(translationQueryModule, queryExpansionModule, documentSearchModule, documentJoinModule, documentPostProcessingModule);
    }

    @Test
    void retrieveDocuments_expandsAndRanks() {
        Query base = new Query("base");
        Query translated = new Query("translated");
        Query expanded = new Query("expanded");
        when(translationQueryModule.translateQuery("base")).thenReturn(translated);
        when(queryExpansionModule.expandQueries(translated)).thenReturn(List.of(expanded));
        when(documentSearchModule.retrieveDocuments(any(Query.class), anyInt(), anyDouble()))
                .thenReturn(List.of(new Document("doc")));
        when(documentJoinModule.joinDocuments(any(Map.class))).thenReturn(List.of(new Document("joined")));
        when(documentPostProcessingModule.rankAndFilterDocuments(any(), anyDouble(), anyInt()))
                .thenReturn(List.of(new Document("ranked")));

        List<Document> result = service.retrieveDocuments(base, 5);

        assertThat(result).hasSize(1);
        verify(documentPostProcessingModule).rankAndFilterDocuments(any(), anyDouble(), anyInt());
    }
}

