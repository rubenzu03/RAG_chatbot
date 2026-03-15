package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.application.ports.output.UserRepositoryPort;
import com.rubenzu03.rag_chatbot.application.ports.input.ManageUserUseCase;
import com.rubenzu03.rag_chatbot.domain.model.UserDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService implements ManageUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepositoryPort userRepositoryPort, PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO registerUser(UserDTO userDto) {
        if (checkUserExists(userDto.getEmail())){
            throw new RuntimeException("User already exists");
        }
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));

        return userRepositoryPort.save(userDto);
    }

    @Override
    public UserDTO getUserProfile(Long id) {
        return userRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public boolean checkUserExists(String email){
        return userRepositoryPort.existsByEmail(email);
    }
}
