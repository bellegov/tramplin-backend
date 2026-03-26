package com.tramplin.backend.dto;
public record AuthResponse(
        String token,
        String role // Добавили роль текстом
) {}