package com.tramplin.backend.dto;

import com.tramplin.backend.model.OpportunityType;
import com.tramplin.backend.model.WorkFormat;

import java.time.LocalDateTime;
import java.util.List;

public record OpportunityCreateRequest(
        String title,
        String description,
        OpportunityType type,
        WorkFormat workFormat,
        String city,
        String exactAddress,
        Double latitude,
        Double longitude,
        LocalDateTime deadline,
        Integer salary,
        List<String> tags
) {}