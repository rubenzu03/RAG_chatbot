package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 1024)
    private String email;
    @Column(unique = true, nullable = false, length = 64)
    private String emailHash;
    @Column(nullable = false)
    private String password;
    private Timestamp createdAt;
    private Timestamp lastLoginAt;
}
