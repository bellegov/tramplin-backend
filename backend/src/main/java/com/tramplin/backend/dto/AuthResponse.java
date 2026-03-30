package com.tramplin.backend.dto;
public record AuthResponse(
        String token,
        String role
) {}