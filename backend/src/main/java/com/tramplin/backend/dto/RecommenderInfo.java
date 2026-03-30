package com.tramplin.backend.dto;

public record RecommenderInfo(
        Long recommenderId,
        String recommenderName,
        String message
) {}