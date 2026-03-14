package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.mapper;

import com.rubenzu03.rag_chatbot.domain.model.User;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        User user = new User();
        user.setId(entity.getId());
        user.setEmail(entity.getEmail());
        user.setPassword(entity.getPassword());
        user.setCreatedAt(entity.getCreatedAt());
        user.setLastLoginAt(entity.getLastLoginAt());
        return user;
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        UserEntity entity = new UserEntity();
        entity.setId(domain.getId());
        entity.setEmail(domain.getEmail());
        entity.setPassword(domain.getPassword());
        if (domain.getCreatedAt() != null) {
            entity.setCreatedAt(domain.getCreatedAt());
        } else {
            entity.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        }
        entity.setLastLoginAt(domain.getLastLoginAt());
        return entity;
    }
}

