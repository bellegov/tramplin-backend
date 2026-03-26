package com.tramplin.backend.dto;

import java.util.List;

public record SeekerProfileDetailedResponse(
        SeekerProfileResponse profile,
        List<ApplicationResponse> applications // Те самые "карьерные интересы"
) {}