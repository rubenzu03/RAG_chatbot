package com.rubenzu03.rag_chatbot.domain.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatResponseTest {

    @Test
    void testChatResponseCreationWithArguments() {
        ChatResponse response = new ChatResponse("Hello bot", "user-123");

        assertThat(response.getResponse()).isEqualTo("Hello bot");
        assertThat(response.getUserId()).isEqualTo("user-123");
    }

    @Test
    void testChatResponseCreationWithoutArguments() {
        ChatResponse response = new ChatResponse();

        assertThat(response.getResponse()).isNull();
        assertThat(response.getUserId()).isNull();
    }

    @Test
    void testChatResponseSetters() {
        ChatResponse response = new ChatResponse();

        response.setResponse("Test message");
        response.setUserId("user-456");

        assertThat(response.getResponse()).isEqualTo("Test message");
        assertThat(response.getUserId()).isEqualTo("user-456");
    }

    @Test
    void testChatResponseWithDifferentMessages() {
        ChatResponse response1 = new ChatResponse("Message 1", "user1");
        ChatResponse response2 = new ChatResponse("Message 2", "user2");

        assertThat(response1.getResponse()).isNotEqualTo(response2.getResponse());
        assertThat(response1.getUserId()).isNotEqualTo(response2.getUserId());
    }

    @Test
    void testChatResponseFieldUpdate() {
        ChatResponse response = new ChatResponse("original", "user-orig");
        response.setResponse("updated");
        response.setUserId("user-updated");

        assertThat(response.getResponse()).isEqualTo("updated");
        assertThat(response.getUserId()).isEqualTo("user-updated");
    }

    @Test
    void testChatResponseWithEmptyString() {
        ChatResponse response = new ChatResponse("", "");

        assertThat(response.getResponse()).isEmpty();
        assertThat(response.getUserId()).isEmpty();
    }

    @Test
    void testChatResponseWithLongMessage() {
        String longMessage = "This is a very long message that contains a lot of text. ".repeat(10);
        ChatResponse response = new ChatResponse(longMessage, "user-long");

        assertThat(response.getResponse()).isEqualTo(longMessage);
        assertThat(response.getUserId()).isEqualTo("user-long");
    }

    @Test
    void testChatResponseWithNullValues() {
        ChatResponse response = new ChatResponse(null, null);

        assertThat(response.getResponse()).isNull();
        assertThat(response.getUserId()).isNull();
    }
}
