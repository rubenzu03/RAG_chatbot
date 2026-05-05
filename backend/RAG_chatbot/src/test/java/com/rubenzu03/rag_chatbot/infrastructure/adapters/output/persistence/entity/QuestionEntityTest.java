package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionEntityTest {

    @Test
    void prePersist_setsIdAndCreatedAtWhenMissing() {
        QuestionEntity entity = new QuestionEntity();

        entity.prePersist();

        assertThat(entity.getId()).isNotBlank();
        assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    void prePersist_keepsExistingValues() {
        QuestionEntity entity = new QuestionEntity();
        entity.setId("q-1");
        Timestamp createdAt = new Timestamp(System.currentTimeMillis());
        entity.setCreatedAt(createdAt);

        entity.prePersist();

        assertThat(entity.getId()).isEqualTo("q-1");
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }
}

