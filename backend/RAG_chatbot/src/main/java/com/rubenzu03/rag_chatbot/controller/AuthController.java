package com.rubenzu03.rag_chatbot.controller;

import com.rubenzu03.rag_chatbot.auth.JwtUtil;
import com.rubenzu03.rag_chatbot.dto.UserDto;
import com.rubenzu03.rag_chatbot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtils;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtils, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @PostMapping("/signin")
    public String authenticateUser(@RequestBody UserDto userDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.email(), userDto.password())
        );
        return jwtUtils.generateToken(authentication.getName());
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody UserDto userDto) {
        if (userService.checkUserExists(userDto.email())) {
            return ResponseEntity.badRequest().body("User already exists");
        }
        userService.createUser(userDto);
        return ResponseEntity.ok("User registered successfully");
    }

}
