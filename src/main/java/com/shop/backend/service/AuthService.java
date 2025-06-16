package com.shop.backend.service;

import com.shop.backend.dto.*;
import com.shop.backend.entity.*;
import com.shop.backend.repository.*;
import com.shop.backend.security.JwtUtil;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public ResponseEntity<?> register(RegisterRequest req) {
        if (!req.getRole().equals("buyer") && !req.getRole().equals("seller")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid role"));
        }

        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already registered"));
        }

        Role role = roleRepository.findByName(req.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .fullName(req.getFullName())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(role)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered"));
    }

    public ResponseEntity<?> login(LoginRequest req, HttpServletResponse response) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid credentials"));
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().getName());

        Cookie cookie = new Cookie("authToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(3600);
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "Login successful"));
    }

    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("authToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        Long userId = Long.parseLong(request.getAttribute("userId").toString());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user);
    }

    public ResponseEntity<?> updateProfile(UpdateProfileRequest req, HttpServletRequest httpRequest) {
        Long userId = Long.parseLong(httpRequest.getAttribute("userId").toString());

        User user = userRepository.findById(userId).orElseThrow();

        if (req.getUsername() != null) user.setUsername(req.getUsername());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getFullName() != null) user.setFullName(req.getFullName());
        if (req.getPassword() != null) user.setPasswordHash(passwordEncoder.encode(req.getPassword()));

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Profile updated", "user", user));
    }
}
