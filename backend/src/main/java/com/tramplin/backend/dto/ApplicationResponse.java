package com.tramplin.backend.dto;


import com.tramplin.backend.model.ApplicationStatus;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Long applicationId,
        Long opportunityId,
        String opportunityTitle,
        String companyName,
        String seekerName,
        String coverLetter,
        ApplicationStatus status,
        LocalDateTime appliedAt
) {}