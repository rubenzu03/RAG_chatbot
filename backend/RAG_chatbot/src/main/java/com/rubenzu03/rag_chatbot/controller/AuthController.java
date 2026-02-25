package com.rubenzu03.rag_chatbot.controller;

import com.rubenzu03.rag_chatbot.auth.JwtUtil;
import com.rubenzu03.rag_chatbot.domain.User;
import com.rubenzu03.rag_chatbot.dto.UserDto;
import com.rubenzu03.rag_chatbot.persistence.UserRepository;
import com.rubenzu03.rag_chatbot.service.UserDetailsService;
import com.rubenzu03.rag_chatbot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private AuthenticationManager authenticationManager;
    private UserService userService;
    private JwtUtil jwtUtils;
    private UserDetailsService userDetailsService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, JwtUtil jwtUtils, UserDetailsService userDetailsService,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    @PostMapping("/signin")
    public String authenticateUser(@RequestBody UserDto userDto){
        UserDetails userDetails = userDetailsService.authUser(userDto);
        return jwtUtils.generateToken(userDetails.getUsername());
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody UserDto userDto){
        if (userService.checkUserExists(userDto.email())){
            return ResponseEntity.badRequest().body("User already exists");
        }
        userService.createUser(userDto);
        return ResponseEntity.ok("User registered successfully");
    }


}
