package com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence;

import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.entity.UserEmailEncryptionKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEmailEncryptionKeyRepository extends JpaRepository<UserEmailEncryptionKeyEntity, String> {
}

