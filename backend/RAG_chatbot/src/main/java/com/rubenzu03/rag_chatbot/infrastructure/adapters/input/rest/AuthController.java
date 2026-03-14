package com.rubenzu03.rag_chatbot.infrastructure.adapters.input.rest;

import com.rubenzu03.rag_chatbot.domain.dto.UserDto;
import com.rubenzu03.rag_chatbot.application.service.AuthenticationManagerService;
import com.rubenzu03.rag_chatbot.application.service.JwtUtilsService;
import com.rubenzu03.rag_chatbot.application.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtilsService jwtUtilsService;
    private final AuthenticationManagerService authenticationManagerService;

    @Autowired
    public AuthController(AuthenticationManagerService authManagerService, JwtUtilsService jwtUtilsService, UserService userService) {
        this.authenticationManagerService = authManagerService;
        this.userService = userService;
        this.jwtUtilsService = jwtUtilsService;
    }

    @PostMapping("/signin")
    public String authenticateUser(@RequestBody UserDto userDto) {
        Authentication authentication = authenticationManagerService.authenticate(userDto.email(), userDto.password());
        return jwtUtilsService.generateToken(authentication.getName());
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
