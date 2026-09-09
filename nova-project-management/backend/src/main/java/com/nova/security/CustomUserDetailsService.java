package com.nova.security;

import com.nova.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public CustomUserDetailsService(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return users.findByEmail(email)
            .map(u -> User.withUsername(u.getEmail()).password(u.getPassword()).roles("USER").build())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
