package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.adapter;

import com.rubenzu03.rag_chatbot.application.ports.output.UserRepositoryPort;
import com.rubenzu03.rag_chatbot.domain.model.UserDTO;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.DataUserRepository;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.UserEntity;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.mapper.UserMapper;
import com.rubenzu03.rag_chatbot.infrastructure.security.UserEmailEncryptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final DataUserRepository dataUserRepository;
    private final UserMapper userMapper;
    private final UserEmailEncryptionService userEmailEncryptionService;

    public UserPersistenceAdapter(DataUserRepository dataUserRepository,
                                  UserMapper userMapper,
                                  UserEmailEncryptionService userEmailEncryptionService) {
        this.dataUserRepository = dataUserRepository;
        this.userMapper = userMapper;
        this.userEmailEncryptionService = userEmailEncryptionService;
    }

    @Override
    @Transactional
    public UserDTO save(UserDTO user) {
        UserEntity entity = userMapper.toEntity(user);
        protectEmailBeforeSave(entity);
        UserEntity savedEntity = dataUserRepository.save(entity);
        return toDtoWithDecryptedEmail(savedEntity);
    }

    @Override
    @Transactional
    public Optional<UserDTO> findByEmail(String email) {
        String normalizedEmail = userEmailEncryptionService.normalizeEmail(email);
        String emailHash = userEmailEncryptionService.hashEmail(normalizedEmail);
        return dataUserRepository.findByEmailHash(emailHash).map(this::toDtoWithDecryptedEmail);
    }

    @Override
    public Optional<UserDTO> findById(Long id) {
        return dataUserRepository.findById(id).map(this::toDtoWithDecryptedEmail);
    }

    @Override
    public boolean existsByEmail(String email) {
        String normalizedEmail = userEmailEncryptionService.normalizeEmail(email);
        String emailHash = userEmailEncryptionService.hashEmail(normalizedEmail);
        return dataUserRepository.existsByEmailHash(emailHash);
    }

    private void protectEmailBeforeSave(UserEntity entity) {
        String normalizedEmail = userEmailEncryptionService.normalizeEmail(entity.getEmail());
        String emailHash = userEmailEncryptionService.hashEmail(normalizedEmail);
        entity.setEmailHash(emailHash);
        entity.setEmail(userEmailEncryptionService.encryptEmail(normalizedEmail));
    }

    private UserDTO toDtoWithDecryptedEmail(UserEntity entity) {
        UserDTO dto = userMapper.toDto(entity);
        dto.setEmail(userEmailEncryptionService.decryptEmail(entity.getEmail(), entity.getEmailHash()));
        return dto;
    }

}

