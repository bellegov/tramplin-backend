package com.tramplin.backend.service;

import com.tramplin.backend.dto.OpportunityCreateRequest;
import com.tramplin.backend.dto.OpportunityDetailedResponse;
import com.tramplin.backend.dto.OpportunityResponse;
import com.tramplin.backend.model.*;
import com.tramplin.backend.model.OpportunityStatus;
import com.tramplin.backend.model.VerificationStatus;
import com.tramplin.backend.model.WorkFormat;
import com.tramplin.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final FavoriteRepository favoriteRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationRepository notificationRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public OpportunityResponse createOpportunity(OpportunityCreateRequest request) {
        User user = getCurrentUser();

        EmployerProfile employer = employerProfileRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Только работодатель может создавать карточки"));

        if (employer.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new RuntimeException("Ваш аккаунт еще не прошел верификацию куратором!");
        }

        Opportunity opportunity = Opportunity.builder()
                .employer(employer)
                .title(request.title())
                .description(request.description())
                .type(request.type())
                .workFormat(request.workFormat())
                .city(request.city())
                .exactAddress(request.exactAddress())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .publishedAt(LocalDateTime.now())
                .deadline(request.deadline())
                .salary(request.salary()) // Устанавливаем зп
                .status(OpportunityStatus.OPEN) // Всегда OPEN при создании
                .build();

        Opportunity saved = opportunityRepository.save(opportunity);

        // Уведомления подписчикам
        var subscribers = subscriptionRepository.findAllByEmployerId(employer.getId());
        for (var sub : subscribers) {
            notificationRepository.save(Notification.builder()
                    .user(sub.getSeeker().getUser())
                    .message("Компания " + employer.getCompanyName() + " опубликовала новую вакансию: " + saved.getTitle())
                    .createdAt(LocalDateTime.now())
                    .isRead(false)
                    .build());
        }

        return mapToResponse(saved, false);
    }

    // ВСПОМОГАТЕЛЬНЫЙ МЕТОД: Получаем ID всех избранных вакансий текущего юзера
    private Set<Long> getFavoriteIdsForUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        Set<Long> favoriteIds = new HashSet<>();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            userRepository.findByEmail(auth.getName()).ifPresent(u ->
                    favoriteIds.addAll(favoriteRepository.findAllByUserId(u.getId()).stream()
                            .map(f -> f.getOpportunity().getId()).collect(Collectors.toSet())));
        }
        return favoriteIds;
    }

    // ОСНОВНОЙ МЕТОД: Поиск и получение списка (с пометкой Избранного!)
    public List<OpportunityResponse> searchOpportunities( String keyword, String city, WorkFormat format, String tag, Integer minSalary) {
        Set<Long> favoriteIds = getFavoriteIdsForUser();

        return opportunityRepository.findByFilters(city, format, tag, minSalary)
                .stream()
                .map(opp -> mapToResponse(opp, favoriteIds.contains(opp.getId())))
                .collect(Collectors.toList());
    }



    // Маппинг легкой карточки (для ленты и карты)
    private OpportunityResponse mapToResponse(Opportunity opp, boolean isFavorite) {
        return new OpportunityResponse(
                opp.getId(), opp.getTitle(), opp.getDescription(), opp.getType(),
                opp.getWorkFormat(), opp.getEmployer().getCompanyName(), opp.getCity(),
                opp.getExactAddress(), opp.getLatitude(), opp.getLongitude(),
                opp.getPublishedAt(), opp.getDeadline(), opp.getStatus(),isFavorite, opp.getSalary(),
                opp.getTags().stream().map(Tag::getName).toList(),
                opp.getImageUrl() // Обложка вакансии
        );
    }

    // Маппинг ЖИРНОЙ карточки (для модального окна)
    private OpportunityDetailedResponse mapToDetailedResponse(Opportunity opp, boolean isFavorite) {
        return new OpportunityDetailedResponse(
                opp.getId(), opp.getTitle(), opp.getDescription(), opp.getType(),
                opp.getWorkFormat(), opp.getCity(), opp.getExactAddress(),
                opp.getLatitude(), opp.getLongitude(), opp.getPublishedAt(),
                opp.getDeadline(), isFavorite, opp.getSalary(),
                opp.getTags().stream().map(Tag::getName).toList(), opp.getStatus(),
                opp.getImageUrl(),

                // Данные работодателя для кружочка и перехода
                opp.getEmployer().getId(),
                opp.getEmployer().getCompanyName(),
                opp.getEmployer().getLogoUrl(),
                opp.getEmployer().getDescription(),
                opp.getEmployer().getWebsite()
        );
    }

// ----------------------------------------------------
// НОВЫЕ МЕТОДЫ ДЛЯ ФОТО И ДЕТАЛЬНОЙ СТРАНИЦЫ
// ----------------------------------------------------

    // Загрузка обложки в вакансию
    public String uploadImage(Long id, String fileUrl) {
        User user = getCurrentUser();
        Opportunity opp = opportunityRepository.findById(id).orElseThrow();
        if (!opp.getEmployer().getId().equals(user.getId())) {
            throw new RuntimeException("Нет прав");
        }
        opp.setImageUrl(fileUrl);
        opportunityRepository.save(opp);
        return fileUrl;
    }

    // Получить детальную вакансию
    public OpportunityDetailedResponse getDetailedById(Long id) {
        Opportunity opp = opportunityRepository.findById(id).orElseThrow();

        // Проверка на лайк (Избранное)
        boolean isFavorite = false;
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            User user = userRepository.findByEmail(auth.getName()).orElseThrow();
            isFavorite = favoriteRepository.findByUserIdAndOpportunityId(user.getId(), id).isPresent();
        }

        return mapToDetailedResponse(opp, isFavorite);
    }



    // 1. Получить список всех СВОИХ вакансий (для ЛК работодателя)
    public List<OpportunityResponse> getMyOpportunities() {
        User user = getCurrentUser();
        Set<Long> favoriteIds = getFavoriteIdsForUser();

        return opportunityRepository.findAllByEmployerId(user.getId())
                .stream()
                .map(opp -> mapToResponse(opp, favoriteIds.contains(opp.getId())))
                .collect(Collectors.toList());
    }

    // 2. Редактирование вакансии (Любые поля)
    @Transactional
    public OpportunityResponse updateOpportunity(Long id, OpportunityCreateRequest request) {
        User user = getCurrentUser();
        Opportunity opp = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Вакансия не найдена"));

        // Проверка прав: редактировать может только владелец или админ
        if (!opp.getEmployer().getId().equals(user.getId()) && user.getRole() != Role.ROLE_ADMIN) {
            throw new RuntimeException("Нет прав на редактирование");
        }

        // Обновляем поля
        opp.setTitle(request.title());
        opp.setDescription(request.description());
        opp.setType(request.type());
        opp.setWorkFormat(request.workFormat());
        opp.setCity(request.city());
        opp.setExactAddress(request.exactAddress());
        opp.setLatitude(request.latitude());
        opp.setLongitude(request.longitude());
        opp.setDeadline(request.deadline());
        opp.setSalary(request.salary());
        // Если нужно менять статус (например из ARCHIVED обратно в OPEN) - можно добавить поле в Request

        Opportunity saved = opportunityRepository.save(opp);
        return mapToResponse(saved, getFavoriteIdsForUser().contains(id));
    }
}