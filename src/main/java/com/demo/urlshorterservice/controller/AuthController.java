package com.demo.urlshorterservice.controller;

import com.demo.urlshorterservice.entity.User;
import com.demo.urlshorterservice.model.AuthRequest;
import com.demo.urlshorterservice.model.AuthResponse;
import com.demo.urlshorterservice.repository.UserRepository;
import com.demo.urlshorterservice.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Register and login to get JWT token")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
        this.jwtUtil=jwtUtil;
    }

    @Operation(summary = "Register a new user",
            description = "Creates account. Use these credentials to login and get JWT.")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest req){

        if(userRepository.existsByUsername(req.getUsername())){
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error","Username already exists"));
        }

        User user = User.builder()
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .role("ROLE_USER")
                .build();


        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message","User registered successfully"));

    }

    @Operation(summary = "Login and get JWT token",
            description = "Returns a Bearer token. Copy it and click Authorize above.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req){
        User user = userRepository.findByUsername(req.getUsername())
                .orElse(null);


        if (user == null || !passwordEncoder.matches(
                req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error","Invalid username or password"));
        }

        String token =  jwtUtil.generateToken(user.getUsername());

        return ResponseEntity.ok(new AuthResponse(
                token,
                user.getUsername(),
                86400000L
        ));
    }

}
