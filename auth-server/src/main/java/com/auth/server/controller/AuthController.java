package com.auth.server.controller;

import com.auth.server.dto.AuthRequest;
import com.auth.server.dto.AuthResponse;
import com.auth.server.entity.User;
import com.auth.server.repository.UserRepository;
import com.auth.server.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, 
                          UserRepository userRepository, 
                          PasswordEncoder passwordEncoder, 
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        // WHY: We hash BEFORE saving to the database so that the plaintext password 
        // never touches the persistent storage, mitigating risk if the DB is compromised.
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles("ROLE_USER");

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRoles());
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            // WHY: We use AuthenticationManager instead of manually pulling the user and 
            // checking the password encoder here. This leverages the full Spring Security 
            // authentication pipeline, ensuring UserDetailsService, PasswordEncoder, and 
            // AccountStatus (locked/disabled) checks are uniformly applied.
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        User user = userRepository.findByUsername(request.username()).orElseThrow();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRoles());
        
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername()));
    }
}
