package com.rubenzu03.rag_chatbot.rag.modules;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.stereotype.Service;

@Service
public class TranslationQueryModule {

    private final ChatClient.Builder chatClientBuilder;

    public TranslationQueryModule(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    public Query translateQuery(String rewrittenQuery){
        Query query = new Query(rewrittenQuery);

        String detectedLanguage = detectLanguage(rewrittenQuery);
        String targetLanguage = "spanish";
        if (detectedLanguage.equalsIgnoreCase(targetLanguage)) {
            targetLanguage = detectedLanguage;
        }
        QueryTransformer queryTransformer = TranslationQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder).targetLanguage(targetLanguage).build();

        return queryTransformer.transform(query);
    }

    private String detectLanguage(String rewrittenQuery){
        LanguageDetector detector = LanguageDetectorBuilder.fromAllLanguages().build();
        Language language = detector.detectLanguageOf(rewrittenQuery);
        return language.name();
    }
}
