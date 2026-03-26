package com.tramplin.backend.dto;

public record SeekerRegisterRequest(
        String email,
        String password,
        String displayName,
        String university
) {}