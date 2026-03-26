package com.tramplin.backend.dto;

public record EmployerProfileUpdateRequest(
        String companyName,
        String inn,
        String description,
        String industry,
        String website
) {}