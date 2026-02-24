package com.rubenzu03.rag_chatbot.service;

import com.rubenzu03.rag_chatbot.components.RAGContextBuilder;
import com.rubenzu03.rag_chatbot.config.ChatClientConfig;
import com.rubenzu03.rag_chatbot.dto.EvaluationRequest;
import com.rubenzu03.rag_chatbot.dto.EvaluationResponse;
import com.rubenzu03.rag_chatbot.dto.QuestionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class QuestionModeService {

    private static Logger log = LoggerFactory.getLogger(QuestionModeService.class);

    private final RetrievalService retrievalService;
    private final RAGContextBuilder ragContextBuilder;
    private final ChatClient chatClient;

    public QuestionModeService(@Qualifier("QuestionModeChatClient")ChatClient chatClient,
                               RetrievalService retrievalService, RAGContextBuilder ragContextBuilder) {
        this.retrievalService = retrievalService;
        this.ragContextBuilder = ragContextBuilder;
        this.chatClient = chatClient;
    }

    public QuestionResponse generateQuestion(){
        List<Document> docs = retrievalService.retrieveDocuments(new Query("*"), 30);

        if (docs.isEmpty()){
            log.error("No documents found");
        }

        Collections.shuffle(docs);
        List<Document> selectedDocs = docs.subList(0, Math.min(3, docs.size()));

        String context = ragContextBuilder.buildRAGContext(selectedDocs);

        String prompt = String.format(ChatClientConfig.QUESTION_GENERATION_PROMPT, context);


        String question = chatClient.prompt()
                .user(prompt)
                .call().content();

        return new QuestionResponse(question,context);
    }

    public EvaluationResponse evaluateAnswer(EvaluationRequest evaluationRequest){
        return null;
    }
}
