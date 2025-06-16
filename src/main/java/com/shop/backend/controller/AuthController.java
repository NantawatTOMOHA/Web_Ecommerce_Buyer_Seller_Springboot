package com.shop.backend.controller;

import com.shop.backend.dto.*;
import com.shop.backend.service.AuthService;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        return authService.login(request, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        System.out.println("Logout API called");
        return authService.logout(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        return authService.getProfile(request);
    }

    @PutMapping("/updateProfile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request, HttpServletRequest httpRequest) {
        return authService.updateProfile(request, httpRequest);
    }
}
