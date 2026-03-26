package com.tramplin.backend.dto;

public record SeekerProfileUpdateRequest(
        String university,
        Integer graduationYear,
        String resumeText,
        String portfolioLinks,
        boolean isPublic
) {}