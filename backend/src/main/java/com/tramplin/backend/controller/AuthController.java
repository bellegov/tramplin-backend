package com.tramplin.backend.controller;

import com.tramplin.backend.dto.*;
import com.tramplin.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/seeker")
    public ResponseEntity<AuthResponse> registerSeeker(@RequestBody SeekerRegisterRequest request) {
        return ResponseEntity.ok(authService.registerSeeker(request));
    }

    @PostMapping("/register/employer")
    public ResponseEntity<AuthResponse> registerEmployer(@RequestBody EmployerRegisterRequest request) {
        return ResponseEntity.ok(authService.registerEmployer(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}