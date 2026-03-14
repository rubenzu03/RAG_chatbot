package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.adapter;

import com.rubenzu03.rag_chatbot.application.ports.output.UserRepositoryPort;
import com.rubenzu03.rag_chatbot.domain.model.User;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.DataUserRepository;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.UserEntity;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final DataUserRepository dataUserRepository;
    private final UserMapper userMapper;

    public UserPersistenceAdapter(DataUserRepository dataUserRepository, UserMapper userMapper) {
        this.dataUserRepository = dataUserRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity saved = dataUserRepository.save(entity);
        userMapper.toDomain(saved);
        return user;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return dataUserRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return dataUserRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return dataUserRepository.existsByEmail(email);
    }
}

