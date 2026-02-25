package com.rubenzu03.rag_chatbot.dto;

import java.io.Serializable;


public record UserDto(String email, String password) implements Serializable {
}