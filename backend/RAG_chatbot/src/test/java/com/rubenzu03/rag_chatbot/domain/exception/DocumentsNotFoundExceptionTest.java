package com.rubenzu03.rag_chatbot.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentsNotFoundExceptionTest {

    @Test
    void constructor_setsMessage() {
        DocumentsNotFoundException exception = new DocumentsNotFoundException("No documents found");

        assertThat(exception.getMessage()).isEqualTo("No documents found");
    }

    @Test
    void extendsRuntimeException() {
        DocumentsNotFoundException exception = new DocumentsNotFoundException("message");

        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
