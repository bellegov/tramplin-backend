package com.tramplin.backend.dto;

import com.tramplin.backend.model.OpportunityStatus;
import com.tramplin.backend.model.OpportunityType;
import com.tramplin.backend.model.WorkFormat;

import java.time.LocalDateTime;
import java.util.List;

public record OpportunityResponse(
        Long id,
        String title,
        String description,
        OpportunityType type,
        WorkFormat workFormat,
        String companyName,
        String city,
        String exactAddress,
        Double latitude,
        Double longitude,
        LocalDateTime publishedAt,
        LocalDateTime deadline,
        OpportunityStatus status,
        boolean isFavorite,
        Integer salary,
        List<String> tags,
        String imageUrl
) {}