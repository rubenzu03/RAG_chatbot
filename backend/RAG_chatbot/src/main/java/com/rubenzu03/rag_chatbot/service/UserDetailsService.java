package com.rubenzu03.rag_chatbot.service;

import com.rubenzu03.rag_chatbot.domain.User;
import com.rubenzu03.rag_chatbot.dto.UserDto;
import com.rubenzu03.rag_chatbot.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsService {

    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;


    @Autowired
    public UserDetailsService(UserRepository userRepository , AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }

    public UserDetails loadUserByUsername(String username){
        try{
            User user = userRepository.findByEmail(username);
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    Collections.emptyList()
            );
        }
        catch (UsernameNotFoundException e){
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }

    public UserDetails authUser(UserDto userDto){
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userDto.email(),userDto.password()
        ));
        return (UserDetails) auth.getPrincipal();
    }
}
