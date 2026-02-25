package com.rubenzu03.rag_chatbot.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity (name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String email;
    private String password;
    private Timestamp createdAt;
    private Timestamp lastLoginAt;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }
}
