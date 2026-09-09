package com.nova.controller;

import com.nova.dto.AuthDtos.*;
import com.nova.entity.User;
import com.nova.repository.UserRepository;
import com.nova.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UserRepository users, BCryptPasswordEncoder encoder, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest r) {
        if (users.findByEmail(r.email()).isPresent())
            return ResponseEntity.badRequest().body("Email already registered");
        User u = User.builder().name(r.name()).email(r.email()).password(encoder.encode(r.password())).build();
        u = users.save(u);
        return ResponseEntity.ok(new AuthResponse(jwt.generate(u.getEmail()), u.getId(), u.getName(), u.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest r) {
        User u = users.findByEmail(r.email()).orElse(null);
        if (u == null || !encoder.matches(r.password(), u.getPassword()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        return ResponseEntity.ok(new AuthResponse(jwt.generate(u.getEmail()), u.getId(), u.getName(), u.getEmail()));
    }
}
