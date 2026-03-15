package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.mapper;

import com.rubenzu03.rag_chatbot.domain.model.UserDTO;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(UserDTO model) {
        if (model == null) {
            return null;
        }
        UserEntity entity = new UserEntity();
        entity.setId(model.getId());
        entity.setEmail(model.getEmail());
        entity.setPassword(model.getPassword());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setLastLoginAt(model.getLastLoginAt());
        return entity;
    }

    public UserDTO toDto(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setId(entity.getId());
        userDTO.setEmail(entity.getEmail());
        userDTO.setPassword(entity.getPassword());
        userDTO.setCreatedAt(entity.getCreatedAt());
        userDTO.setLastLoginAt(entity.getLastLoginAt());
        return userDTO;
    }
}

