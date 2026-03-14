package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.domain.model.User;
import com.rubenzu03.rag_chatbot.application.ports.input.ManageUserUseCase;
import com.rubenzu03.rag_chatbot.domain.dto.UserDto;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.DataUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements ManageUserUseCase {

    private final DataUserRepository dataUserRepostory;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(DataUserRepository dataUserRepostory, PasswordEncoder passwordEncoder) {
        this.dataUserRepostory = dataUserRepostory;
        this.passwordEncoder = passwordEncoder;
    }

    public void createUser(UserDto userDto) {

    }

    @Override
    public User registerUser(UserDto userDto) {
        if (checkUserExists(userDto.email())){
            throw new RuntimeException("User already exists");
        }

        User newUser = new User(userDto.email(), passwordEncoder.encode(userDto.password()));
        dataUserRepostory.save(newUser);
        UserDto newUserDto = new UserDto(newUser.getEmail(), newUser.getPassword());
    }

    @Override
    public User getUserProfile(Long id) {
        return null;
    }

    public boolean checkUserExists(String email){
        return dataUserRepostory.existsByEmail(email);
    }
}
