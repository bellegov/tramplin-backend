package com.tramplin.backend.dto;

public record CreateCuratorRequest(
        String email,
        String password,
        String displayName
) {}