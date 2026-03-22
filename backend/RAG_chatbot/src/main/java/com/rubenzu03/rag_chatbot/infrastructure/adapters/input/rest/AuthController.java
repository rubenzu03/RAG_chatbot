package com.rubenzu03.rag_chatbot.infrastructure.adapters.input.rest;

import com.rubenzu03.rag_chatbot.application.ports.input.ManageUserUseCase;
import com.rubenzu03.rag_chatbot.domain.model.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ManageUserUseCase manageUserUseCase;

    @Autowired
    public AuthController(ManageUserUseCase manageUserUseCase) {
        this.manageUserUseCase = manageUserUseCase;
    }

    @PostMapping("/signin")
    public String authenticateUser(@RequestBody UserDTO userDto) {
        return manageUserUseCase.login(userDto.getEmail(), userDto.getPassword());
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDto) {
        if (manageUserUseCase.checkUserExists(userDto.getEmail())) {
            return ResponseEntity.badRequest().body("User already exists");
        }
        manageUserUseCase.registerUser(userDto);
        return ResponseEntity.ok("User registered successfully");
    }

}
