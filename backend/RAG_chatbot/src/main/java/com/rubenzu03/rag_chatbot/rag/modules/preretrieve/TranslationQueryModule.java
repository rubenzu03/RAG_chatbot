package com.rubenzu03.rag_chatbot.rag.modules.preretrieve;

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

    private static final String TARGET_LANGUAGE = "spanish";

    private final LanguageDetector languageDetector;
    private final QueryTransformer queryTransformer;

    public TranslationQueryModule(ChatClient.Builder chatClientBuilder) {
        this.languageDetector = LanguageDetectorBuilder.fromAllLanguages().build();
        this.queryTransformer = TranslationQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .targetLanguage(TARGET_LANGUAGE)
                .build();
    }

    public Query translateQuery(String rewrittenQuery) {
        Query query = new Query(rewrittenQuery);

        String detectedLanguage = detectLanguage(rewrittenQuery);

        if (!detectedLanguage.equalsIgnoreCase(TARGET_LANGUAGE)) {
            return queryTransformer.transform(query);
        }

        return query;
    }

    private String detectLanguage(String text) {
        Language language = languageDetector.detectLanguageOf(text);
        return language.name();
    }
}
