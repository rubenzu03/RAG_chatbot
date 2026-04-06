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
            You are a strict, objective teacher grading a student's answer.
            
            Context (The Absolute Truth):
            %s
            
            Question:
            %s
            
            Student Answer:
            %s
            
            INSTRUCTIONS (CRITICAL):
            You must evaluate if the Student Answer is correct based ONLY on the Context.
            You MUST return EXACTLY ONE valid JSON object and NOTHING ELSE. No markdown fences, no extra text.
            If the user states ignorance, automatically grade it as INCORRECT
            
            1) JSON SCHEMA (Strict):
            {
              "analysis": "<Draft your internal reasoning first. Explicitly compare the Student Answer against the Context. Note any contradictions or missing facts.>",
              "result": "<CORRECT|INCORRECT|PARTIAL>",
              "explanation": "<First: brief reason for the label based ONLY on the Context. Second: the expected/canonical answer. Separate both parts with ' | Expected: '>"
            }
            
            2) THE 'result' RULE (NON-NEGOTIABLE):
            - The value for 'result' MUST be exactly one of: CORRECT, INCORRECT, PARTIAL
            - DO NOT put any explanation text in 'result'.
            
            3) GRADING RULES:
            - INCORRECT: The Student Answer contradicts the Context, introduces false facts, or explicitly states ignorance ("I don't know", "skip"). If the Context says X and the student says Y, it is INCORRECT.
            - PARTIAL: The Student Answer is correct but incomplete according to the Context.
            - CORRECT: The Student Answer fully and accurately matches the Context.
            
            4) EXAMPLES (WRONG vs EXACT):
            
            Context: "Hexagonal Architecture improves maintainability by decoupling the core."
            Question: "What is the benefit of Hexagonal Architecture?"
            Student Answer: "It improves performance."
            OUTPUT:
            {"analysis":"Student claims performance is improved, but the context states it improves maintainability. This is a direct contradiction.","result":"INCORRECT","explanation":"Contradicted by context: 'improves maintainability' | Expected: Hexagonal Architecture improves maintainability by decoupling the core."}
            
            Context: "La fotosíntesis convierte energía luminosa en química."
            Question: "¿Qué hace la fotosíntesis?"
            Student Answer: "Convierte energía luminosa a química."
            OUTPUT:
            {"analysis":"The student's answer perfectly matches the physical process described in the context.","result":"CORRECT","explanation":"Matches context: 'convierte energía luminosa en energía química' | Expected: Photosynthesis converts light energy into chemical energy."}
            
            FINAL WARNING:
            Output ONLY the JSON object. Do not include markdown output formatting like ```json.
            """;

    @Bean("AnswerModeChatClient")
    public ChatClient AnswerModeChatClient(OllamaChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultSystem(ANSWER_MODE_GENERATION_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Bean("QuestionModeChatClient")
    public ChatClient QuestionModeChatClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    @Bean("EvaluationChatClient")
    public ChatClient EvaluationChatClient(OllamaChatModel chatModel) {
        return ChatClient.builder(chatModel).defaultSystem(EVALUATION_PROMPT).build();
    }


}
