package com.example.samloauthexample.service.impl;

import com.example.samloauthexample.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws
            UsernameNotFoundException {
        // get user by user name
        User user = new User();
        user.setUsername(username);
//        user = userRepository.findValidUserByName(username);
        return user;
    }
}
