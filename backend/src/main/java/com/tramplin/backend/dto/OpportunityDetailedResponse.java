package com.tramplin.backend.dto;



import com.tramplin.backend.model.OpportunityStatus;
import com.tramplin.backend.model.OpportunityType;
import com.tramplin.backend.model.WorkFormat;

import java.time.LocalDateTime;
import java.util.List;

public record OpportunityDetailedResponse(
        Long id,
        String title,
        String description,
        OpportunityType type,
        WorkFormat workFormat,
        String city,
        String exactAddress,
        Double latitude,
        Double longitude,
        LocalDateTime publishedAt,
        LocalDateTime deadline,
        boolean isFavorite,
        Integer salary,
        List<String> tags,
        OpportunityStatus status,
        String imageUrl,          // Обложка вакансии (большая)

        // Блок инфы о работодателе для модалки:
        Long employerId,          // Чтобы перейти в профиль
        String companyName,       // Название
        String employerLogoUrl,   // Аватарка (круглыш)
        String companyDescription,// Кратко о компании
        String website            // Ссылка на их сайт
) {}