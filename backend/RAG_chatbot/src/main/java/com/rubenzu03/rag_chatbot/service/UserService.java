package com.rubenzu03.rag_chatbot.service;

import com.rubenzu03.rag_chatbot.domain.User;
import com.rubenzu03.rag_chatbot.dto.UserDto;
import com.rubenzu03.rag_chatbot.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void createUser(UserDto userDto) {
        if (checkUserExists(userDto.email())){
            throw new RuntimeException("User already exists");
        }

        User newUser = new User(userDto.email(), passwordEncoder.encode(userDto.password()));
        userRepository.save(newUser);
        UserDto newUserDto = new UserDto(newUser.getEmail(), newUser.getPassword());
    }

    public boolean checkUserExists(String email){
        return userRepository.existsByEmail(email);
    }
}
