package com.tramplin.backend.service;

import com.tramplin.backend.dto.EmployerProfileResponse;
import com.tramplin.backend.dto.EmployerProfileUpdateRequest;
import com.tramplin.backend.model.EmployerProfile;
import com.tramplin.backend.model.User;
import com.tramplin.backend.repository.ApplicationRepository;
import com.tramplin.backend.repository.EmployerProfileRepository;
import com.tramplin.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmployerService {

    private final EmployerProfileRepository employerProfileRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final SeekerService seekerService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }


    public EmployerProfileResponse updateProfile(EmployerProfileUpdateRequest request) {
        User user = getCurrentUser();
        EmployerProfile profile = employerProfileRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Профиль работодателя не найден"));

        profile.setCompanyName(request.companyName());
        profile.setInn(request.inn());
        profile.setDescription(request.description());
        profile.setIndustry(request.industry());
        profile.setWebsite(request.website());


        EmployerProfile saved = employerProfileRepository.save(profile);
        return mapToResponse(saved);
    }
    public String updateLogo(String fileUrl) {
        User user = getCurrentUser();
        EmployerProfile profile = employerProfileRepository.findById(user.getId()).orElseThrow();
        profile.setLogoUrl(fileUrl);
        employerProfileRepository.save(profile);
        return fileUrl;
    }

    public EmployerProfileResponse getMyProfile() {
        User user = getCurrentUser();
        EmployerProfile profile = employerProfileRepository.findById(user.getId()).orElseThrow();
        return mapToResponse(profile);
    }


    // 1. Посмотреть отклики на свои вакансии
    public java.util.List<com.tramplin.backend.dto.ApplicationResponse> getApplicationsForMyOpportunities() {
        User user = getCurrentUser();
        return applicationRepository.findAllByOpportunityEmployerId(user.getId())
                .stream()
                .map(seekerService::mapApplicationResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    // 2. Изменить статус отклика (Принять/Отклонить/Резерв)
    public com.tramplin.backend.dto.ApplicationResponse changeApplicationStatus(Long applicationId, com.tramplin.backend.model.ApplicationStatus newStatus) {
        User user = getCurrentUser();
        com.tramplin.backend.model.entity.Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Отклик не найден"));


        if (!application.getOpportunity().getEmployer().getId().equals(user.getId())) {
            throw new RuntimeException("Нет доступа к этому отклику");
        }

        application.setStatus(newStatus);
        applicationRepository.save(application);
        return seekerService.mapApplicationResponse(application);
    }


    private EmployerProfileResponse mapToResponse(EmployerProfile profile) {
        return new EmployerProfileResponse(
                profile.getId(),
                profile.getCompanyName(),
                profile.getInn(),
                profile.getDescription(),
                profile.getIndustry(),
                profile.getWebsite(),
                profile.getVerificationStatus(),
                profile.getLogoUrl()
        );
    }
}