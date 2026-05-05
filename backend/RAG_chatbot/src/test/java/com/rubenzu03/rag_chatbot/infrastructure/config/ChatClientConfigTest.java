package com.rubenzu03.rag_chatbot.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ChatClientConfigTest {

    private ChatClientConfig config;

    @Mock
    private OllamaChatModel chatModel;

    @Mock
    private ChatMemory chatMemory;

    @Test
    void testAnswerModeChatClientBeanCreation() {
        config = new ChatClientConfig();
        ChatClient answerClient = config.AnswerModeChatClient(chatModel, chatMemory);

        assertThat(answerClient).isNotNull();
    }

    @Test
    void testQuestionModeChatClientBeanCreation() {
        config = new ChatClientConfig();
        ChatClient questionClient = config.QuestionModeChatClient(chatModel);
        assertThat(questionClient).isNotNull();
    }

    @Test
    void testEvaluationChatClientBeanCreation() {
        config = new ChatClientConfig();
        ChatClient evaluationClient = config.EvaluationChatClient(chatModel);
        assertThat(evaluationClient).isNotNull();
    }

    @Test
    void testAnswerModeGenerationPromptIsNotEmpty() {
        assertThat(ChatClientConfig.ANSWER_MODE_GENERATION_PROMPT).isNotEmpty();
        assertThat(ChatClientConfig.ANSWER_MODE_GENERATION_PROMPT).contains("helpful", "study assistant");
    }

    @Test
    void testQuestionGenerationPromptIsNotEmpty() {
        assertThat(ChatClientConfig.QUESTION_GENERATION_PROMPT).isNotEmpty();
        assertThat(ChatClientConfig.QUESTION_GENERATION_PROMPT).contains("clear", "concrete", "question");
    }

    @Test
    void testEvaluationPromptIsNotEmpty() {
        assertThat(ChatClientConfig.EVALUATION_PROMPT).isNotEmpty();
        assertThat(ChatClientConfig.EVALUATION_PROMPT).contains("JSON", "result", "CORRECT");
    }

    @Test
    void testEvaluationPromptContainsAllRequiredGrades() {
        assertThat(ChatClientConfig.EVALUATION_PROMPT)
                .contains("CORRECT")
                .contains("INCORRECT")
                .contains("PARTIAL");
    }

    @Test
    void testPromptTemplateFormatting() {
        assertThat(ChatClientConfig.QUESTION_GENERATION_PROMPT).contains("%s");
        assertThat(ChatClientConfig.EVALUATION_PROMPT).contains("%s");
    }
}
