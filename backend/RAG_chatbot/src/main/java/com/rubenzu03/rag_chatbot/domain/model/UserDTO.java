package com.rubenzu03.rag_chatbot.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String password;
    private Timestamp createdAt;
    private Timestamp lastLoginAt;

    public UserDTO(String email, String password) {
        this.email = email;
        this.password = password;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.lastLoginAt = new Timestamp(System.currentTimeMillis());
    }
}
