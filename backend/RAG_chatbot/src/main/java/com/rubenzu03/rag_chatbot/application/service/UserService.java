package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.application.ports.output.UserRepositoryPort;
import com.rubenzu03.rag_chatbot.domain.model.User;
import com.rubenzu03.rag_chatbot.application.ports.input.ManageUserUseCase;
import com.rubenzu03.rag_chatbot.domain.dto.UserDto;
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
    public UserDto registerUser(UserDto userDto) {
        if (checkUserExists(userDto.email())){
            throw new RuntimeException("User already exists");
        }

        User newUser = new User(userDto.email(), passwordEncoder.encode(userDto.password()));
        UserDto newUserDto = new UserDto(newUser.getEmail(), newUser.getPassword());
        userRepositoryPort.save(newUser);
        return newUserDto;
    }

    @Override
    public UserDto getUserProfile(Long id) {
        return null;
    }

    public boolean checkUserExists(String email){
        return userRepositoryPort.existsByEmail(email);
    }
}
