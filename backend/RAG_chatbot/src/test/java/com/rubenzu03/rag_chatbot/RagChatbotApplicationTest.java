package com.rubenzu03.rag_chatbot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.assertj.core.api.Assertions.assertThat;

class RagChatbotApplicationTest {

    @Test
    void classHasSpringBootApplicationAnnotation() {
        assertThat(RagChatbotApplication.class.isAnnotationPresent(SpringBootApplication.class)).isTrue();
    }
}
