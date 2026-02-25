package com.rubenzu03.rag_chatbot.service;

import com.rubenzu03.rag_chatbot.ragmodules.preretrieve.QueryTransformerModule;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransformQueryService {

    private final QueryTransformerModule queryTransformer;

    @Autowired
    public TransformQueryService(QueryTransformerModule queryTransformer) {
        this.queryTransformer = queryTransformer;
    }

    public Query transformQuery(Query query, String sessionId){
        return queryTransformer.transformQuery(query.text(), sessionId);
    }
}
