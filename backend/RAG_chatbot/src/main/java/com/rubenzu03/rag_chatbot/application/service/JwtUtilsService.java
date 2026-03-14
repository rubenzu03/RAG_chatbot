package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.auth.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JwtUtilsService {

    private final JwtUtil jwtUtils;

    @Autowired
    public JwtUtilsService(JwtUtil jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    public String generateToken(String username) {
        return jwtUtils.generateToken(username);
    }

}
