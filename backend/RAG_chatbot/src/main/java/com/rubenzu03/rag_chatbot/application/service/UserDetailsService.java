package com.rubenzu03.rag_chatbot.application.service;

import com.rubenzu03.rag_chatbot.domain.model.User;
import com.rubenzu03.rag_chatbot.infrastructure.adapters.output.persistence.DataUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final DataUserRepository dataUserRepostory;

    @Autowired
    public UserDetailsService(DataUserRepository dataUserRepostory) {
        this.dataUserRepostory = dataUserRepostory;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = dataUserRepostory.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.emptyList()
        );
    }
}
