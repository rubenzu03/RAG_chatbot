package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.application.ports.output.UserRepositoryPort;
import com.rubenzu03.rag_chatbot.application.ports.input.ManageUserUseCase;
import com.rubenzu03.rag_chatbot.domain.model.UserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;


@Service
public class UserService implements ManageUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerService authenticationManagerService;
    private final JwtUtilsService jwtUtilsService;

    public UserService(UserRepositoryPort userRepositoryPort, PasswordEncoder passwordEncoder,
                       AuthenticationManagerService authenticationManagerService,
                       JwtUtilsService jwtUtilsService) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManagerService = authenticationManagerService;
        this.jwtUtilsService = jwtUtilsService;
    }

    @Override
    public String login(String email, String password) {
        Authentication authentication = authenticationManagerService.authenticate(email, password);
        Optional<UserDTO> retrievedUser = userRepositoryPort.findByEmail(email);
        retrievedUser.ifPresent(user -> {
            user.setLastLoginAt(new Timestamp(System.currentTimeMillis()));
            userRepositoryPort.save(user);
        });
        return jwtUtilsService.generateToken(authentication.getName());
    }

    @Override
    public UserDTO registerUser(UserDTO userDto) {
        if (checkUserExists(userDto.getEmail())){
            throw new RuntimeException("User already exists");
        }
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userDto.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        userDto.setLastLoginAt(new Timestamp(System.currentTimeMillis()));

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
