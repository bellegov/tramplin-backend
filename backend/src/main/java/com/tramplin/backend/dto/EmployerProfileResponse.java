package com.tramplin.backend.dto;

import com.tramplin.backend.model.VerificationStatus;

public record EmployerProfileResponse(
        Long id,
        String companyName,
        String inn,
        String description,
        String industry,
        String website,
        VerificationStatus verificationStatus,
        String logoUrl
) {}