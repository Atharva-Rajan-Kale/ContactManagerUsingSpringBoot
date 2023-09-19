package com.example.springbootproject.config;

import com.example.springbootproject.dao.Repository;
import com.example.springbootproject.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    private Repository repository;

    @Override
    public UserDetails loadUserByUsername(String username){
        CustomUserDetails customUserDetails;
        User user=repository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return new CustomUserDetails(user);
    }
}
