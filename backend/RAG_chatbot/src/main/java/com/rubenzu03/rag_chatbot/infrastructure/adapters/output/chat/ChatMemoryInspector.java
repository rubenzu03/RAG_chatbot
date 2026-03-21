package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.chat;

import org.springframework.ai.chat.memory.ChatMemory;
import java.lang.reflect.Method;

public class ChatMemoryInspector {
    public static void main(String[] args) {
        for (Method m : ChatMemory.class.getMethods()) {
            System.out.println(m.toString());
        }
    }
}

