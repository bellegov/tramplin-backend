package com.tramplin.backend.service;

import com.tramplin.backend.dto.ApplicationResponse;
import com.tramplin.backend.dto.SeekerProfileDetailedResponse;
import com.tramplin.backend.dto.SeekerProfileResponse;
import com.tramplin.backend.dto.SeekerProfileUpdateRequest;
import com.tramplin.backend.model.*;
import com.tramplin.backend.model.entity.Application;
import com.tramplin.backend.repository.ApplicationRepository;
import com.tramplin.backend.repository.OpportunityRepository;
import com.tramplin.backend.repository.SeekerProfileRepository;
import com.tramplin.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeekerService {

    private final SeekerProfileRepository seekerRepository;
    private final ApplicationRepository applicationRepository;
    private final OpportunityRepository opportunityRepository;
    private final UserRepository userRepository;
    private final com.tramplin.backend.repository.FriendshipRepository friendshipRepo;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    // 1. Обновление резюме и профиля
    public SeekerProfileResponse updateProfile(SeekerProfileUpdateRequest request) {
        User user = getCurrentUser();
        SeekerProfile profile = seekerRepository.findById(user.getId()).orElseThrow();

        profile.setUniversity(request.university());
        profile.setGraduationYear(request.graduationYear());
        profile.setResumeText(request.resumeText());
        profile.setPortfolioLinks(request.portfolioLinks());
        profile.setPublic(request.isPublic());

        seekerRepository.save(profile);
        return mapToResponse(profile);
    }
    public String updateAvatar(String fileUrl) {
        User user = getCurrentUser();
        SeekerProfile profile = seekerRepository.findById(user.getId()).orElseThrow();
        profile.setAvatarUrl(fileUrl);
        seekerRepository.save(profile);
        return fileUrl;
    }

    // 2. Отклик на вакансию
    public ApplicationResponse applyForOpportunity(Long opportunityId, String coverLetter) {
        User user = getCurrentUser();
        SeekerProfile seeker = seekerRepository.findById(user.getId()).orElseThrow();

        if (applicationRepository.existsBySeekerIdAndOpportunityId(seeker.getId(), opportunityId)) {
            throw new RuntimeException("Вы уже откликались на эту вакансию!");
        }

        Opportunity opportunity = opportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new RuntimeException("Вакансия не найдена"));

        // ГЛАВНАЯ ПРОВЕРКА:
        if (opportunity.getStatus() != OpportunityStatus.OPEN) {
            throw new RuntimeException("К сожалению, эта возможность уже закрыта или удалена.");
        }

        Application application = Application.builder()
                .seeker(seeker)
                .opportunity(opportunity)
                .coverLetter(coverLetter)
                .status(ApplicationStatus.APPLIED) // Статус по умолчанию
                .appliedAt(LocalDateTime.now())
                .build();

        Application saved = applicationRepository.save(application);
        return mapApplicationResponse(saved);
    }

    // 3. Просмотр своих откликов
    public List<ApplicationResponse> getMyApplications() {
        User user = getCurrentUser();
        return applicationRepository.findAllBySeekerId(user.getId())
                .stream().map(this::mapApplicationResponse).collect(Collectors.toList());
    }

    public SeekerProfileDetailedResponse getDetailedProfile(Long targetSeekerId) {
        User currentUser = getCurrentUser();
        SeekerProfile target = seekerRepository.findById(targetSeekerId)
                .orElseThrow(() -> new RuntimeException("Соискатель не найден"));

        // Проверяем дружбу (в обе стороны)
        boolean isFriend = friendshipRepo.existsBySeekerIdAndFriendId(currentUser.getId(), targetSeekerId)
                || friendshipRepo.existsBySeekerIdAndFriendId(targetSeekerId, currentUser.getId());

        // Если не публичный, не друг и не админ - от ворот поворот
        if (!target.isPublic() && !isFriend && currentUser.getRole() != Role.ROLE_ADMIN) {
            throw new RuntimeException("Этот профиль скрыт настройками приватности");
        }

        // Собираем базовую инфу
        SeekerProfileResponse baseInfo = mapToResponse(target);

        // Собираем отклики (карьерные интересы)
        List<ApplicationResponse> apps = applicationRepository.findAllBySeekerId(targetSeekerId)
                .stream()
                .map(this::mapApplicationResponse)
                .collect(Collectors.toList());

        return new SeekerProfileDetailedResponse(baseInfo, apps);
    }

    public SeekerProfileResponse mapToResponse(SeekerProfile profile) {
        return new SeekerProfileResponse(
                profile.getId(),
                profile.getUser().getDisplayName(),
                profile.getUser().getEmail(),
                profile.getUniversity(),
                profile.getGraduationYear(),
                profile.getResumeText(),
                profile.getPortfolioLinks(),
                profile.isPublic(),
                profile.getAvatarUrl()
        );
    }

    public ApplicationResponse mapApplicationResponse(Application app) {
        return new ApplicationResponse(
                app.getId(),
                app.getOpportunity().getId(),
                app.getOpportunity().getEmployer().getId(),
                app.getSeeker().getId(),
                app.getOpportunity().getTitle(),
                app.getOpportunity().getEmployer().getCompanyName(),
                app.getSeeker().getUser().getDisplayName(),
                app.getCoverLetter(),
                app.getStatus(),
                app.getAppliedAt()
        );
    }
}