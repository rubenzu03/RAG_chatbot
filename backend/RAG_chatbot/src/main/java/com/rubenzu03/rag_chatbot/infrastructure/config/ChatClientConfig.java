package com.rubenzu03.rag_chatbot.infrastructure.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {


    public static final String ANSWER_MODE_GENERATION_PROMPT = """
            You are a helpful and precise study assistant for students. Your task is to answer the user's question.
            You must be professional, friendly, and informal. Try to be as objective as possible.
            If the user tries to ask about inappropriate, offensive, or illegal topics,
            you must politely reject the request and mention that you are only a study assistant.
            Never mention where the files you worked with are located.

            Reply in the same language as the user's query.\s
           \s""";

    public static final String QUESTION_GENERATION_PROMPT = """
            Context:
            %s

            Generate ONE clear and concrete study question based only on the context.
            The question must:
            - Assess conceptual understanding, not literal memorization
            - Have an answer that can be verified in the context

            Return ONLY the question, with no additional explanations.
            """;

    public static final String EVALUATION_PROMPT = """
            Context:
            %s

            Question:
            %s

            Answer:
            %s

            Evaluate the answer following these rules:
            1. First indicate whether it is CORRECT or INCORRECT.
            2. Explain why based only on the context.
            3. If the answer is partially correct, state it clearly.
            4. Do not introduce information that is not in the context.
            5. If the received answer suggests the user does not know the answer, evaluate it as INCORRECT.

            Output format:
            - Result: CORRECT | INCORRECT | PARTIAL
            - Explanation: brief and clear text
            """;

    @Bean("AnswerModeChatClient")
    public ChatClient AnswerModeChatClient(OllamaChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultSystem(ANSWER_MODE_GENERATION_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Bean("QuestionModeChatClient")
    public ChatClient QuestionModeChatClient(OllamaChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel).build();
    }


}
