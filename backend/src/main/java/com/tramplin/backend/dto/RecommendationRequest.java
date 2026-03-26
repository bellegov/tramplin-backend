package com.tramplin.backend.dto;

public record RecommendationRequest(
        Long friendId,
        Long opportunityId,
        String message
) {}