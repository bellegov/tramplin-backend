package com.tramplin.backend.dto;
import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String message,
        boolean isRead,
        LocalDateTime createdAt
) {}