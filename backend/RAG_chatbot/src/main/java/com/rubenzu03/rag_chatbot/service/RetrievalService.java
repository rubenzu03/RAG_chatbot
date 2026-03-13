package com.rubenzu03.rag_chatbot.service;

import com.rubenzu03.rag_chatbot.ragmodules.postretrieve.DocumentPostProcessingModule;
import com.rubenzu03.rag_chatbot.ragmodules.preretrieve.QueryExpansionModule;
import com.rubenzu03.rag_chatbot.ragmodules.preretrieve.QueryTransformerModule;
import com.rubenzu03.rag_chatbot.ragmodules.preretrieve.RewriteQueryModule;
import com.rubenzu03.rag_chatbot.ragmodules.preretrieve.TranslationQueryModule;
import com.rubenzu03.rag_chatbot.ragmodules.retrieve.DocumentJoinModule;
import com.rubenzu03.rag_chatbot.ragmodules.retrieve.DocumentSearchModule;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RetrievalService {

    private final QueryTransformerModule queryTransformerModule;
    private final RewriteQueryModule rewriteQueryModule;
    private final TranslationQueryModule translationQueryModule;
    private final QueryExpansionModule queryExpansionModule;
    private final DocumentSearchModule documentSearchModule;
    private final DocumentJoinModule documentJoinModule;
    private final DocumentPostProcessingModule documentPostProcessingModule;

    public RetrievalService(QueryTransformerModule queryTransformerModule, RewriteQueryModule rewriteQueryModule, TranslationQueryModule translationQueryModule, QueryExpansionModule queryExpansionModule, DocumentSearchModule documentSearchModule, DocumentJoinModule documentJoinModule, DocumentPostProcessingModule documentPostProcessingModule) {
        this.queryTransformerModule = queryTransformerModule;
        this.rewriteQueryModule = rewriteQueryModule;
        this.translationQueryModule = translationQueryModule;
        this.queryExpansionModule = queryExpansionModule;
        this.documentSearchModule = documentSearchModule;
        this.documentJoinModule = documentJoinModule;
        this.documentPostProcessingModule = documentPostProcessingModule;
    }


    public List<Document> retrieveDocuments(Query finalQuery, int topK) {
        finalQuery = rewriteQueryModule.rewriteUserQuery(finalQuery.text());
        finalQuery = translationQueryModule.translateQuery(finalQuery.text());

        List<Query> expandedQueries = queryExpansionModule.expandQueries(finalQuery);
        if (!expandedQueries.contains(finalQuery)) {
            List<Query> allQueries = new ArrayList<>(expandedQueries);
            allQueries.addFirst(finalQuery);
            expandedQueries = allQueries;
        }

        Map<Query, List<List<Document>>> queryToDocuments = new HashMap<>();
        for (Query expandedQuery : expandedQueries) {
            List<Document> retrievedDocs = documentSearchModule.retrieveDocuments(expandedQuery, 20, 0.3);
            queryToDocuments.put(expandedQuery, List.of(retrievedDocs));
        }

        List<Document> joinedDocs = documentJoinModule.joinDocuments(queryToDocuments);

        return documentPostProcessingModule.rankAndFilterDocuments(
                joinedDocs,
                0.4,
                topK
        );
    }


}
