package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence;

import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DataUserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmailHash(String emailHash);
    Optional<UserEntity> findByEmailHash(String emailHash);
}
