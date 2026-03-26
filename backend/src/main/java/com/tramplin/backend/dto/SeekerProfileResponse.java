package com.tramplin.backend.dto;

public record SeekerProfileResponse(
        Long id,
        String displayName,
        String email,
        String university,
        Integer graduationYear,
        String resumeText,
        String portfolioLinks,
        boolean isPublic,
        String avatarUrl
) {}