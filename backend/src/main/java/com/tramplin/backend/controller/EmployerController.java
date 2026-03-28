package com.tramplin.backend.controller;

import com.tramplin.backend.dto.ApplicationResponse;
import com.tramplin.backend.dto.EmployerProfileResponse;
import com.tramplin.backend.dto.EmployerProfileUpdateRequest;
import com.tramplin.backend.dto.OpportunityResponse;
import com.tramplin.backend.model.*;
import com.tramplin.backend.repository.EmployerProfileRepository;
import com.tramplin.backend.repository.SeekerProfileRepository;
import com.tramplin.backend.repository.SubscriptionRepository;
import com.tramplin.backend.repository.UserRepository;
import com.tramplin.backend.service.EmployerService;
import com.tramplin.backend.service.MinioService;
import com.tramplin.backend.service.OpportunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employers") // ИСПРАВЛЕН БАЗОВЫЙ ПУТЬ
@RequiredArgsConstructor
public class EmployerController {

    private final EmployerService employerService;
    private final EmployerProfileRepository employerProfileRepository;
    private final MinioService minioService;
    private final UserRepository userRepository;
    private final OpportunityService opportunityService;
    private final  SeekerProfileRepository seekerProfileRepository;
    private final SubscriptionRepository subscriptionRepository;

    // --- ЛИЧНЫЙ ПРОФИЛЬ ---

    @GetMapping("/profile")
    public ResponseEntity<EmployerProfileResponse> getMyProfile() {
        return ResponseEntity.ok(employerService.getMyProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<EmployerProfileResponse> updateProfile(@RequestBody EmployerProfileUpdateRequest request) {
        return ResponseEntity.ok(employerService.updateProfile(request));
    }

    @PostMapping(value = "/profile/logo", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadLogo(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        String fileUrl = minioService.uploadFile(file);
        return ResponseEntity.ok(employerService.updateLogo(fileUrl));
    }

    @DeleteMapping("/profile/logo")
    public ResponseEntity<String> deleteLogo() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        EmployerProfile profile = employerProfileRepository.findById(user.getId()).orElseThrow();

        profile.setLogoUrl(null);
        employerProfileRepository.save(profile);
        return ResponseEntity.ok("Логотип удален");
    }

    // --- ОТКЛИКИ И ВАКАНСИИ ---

    @GetMapping("/profile/applications")
    public ResponseEntity<List<ApplicationResponse>> getApplications() {
        return ResponseEntity.ok(employerService.getApplicationsForMyOpportunities());
    }

    @PatchMapping("/profile/applications/{applicationId}/status")
    public ResponseEntity<com.tramplin.backend.dto.ApplicationResponse> changeStatus(
            @PathVariable Long applicationId,
            @RequestParam ApplicationStatus status) {
        return ResponseEntity.ok(employerService.changeApplicationStatus(applicationId, status));
    }

    @GetMapping("/my-opportunities")
    public ResponseEntity<List<OpportunityResponse>> getMyOpportunities() {
        return ResponseEntity.ok(opportunityService.getMyOpportunities());
    }

    // --- ПУБЛИЧНЫЕ РУЧКИ ---

    @GetMapping("/{id}")
    public ResponseEntity<EmployerProfileResponse> getEmployerPublicProfile(@PathVariable Long id) {
        EmployerProfile profile = employerProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Компания не найдена"));

        return ResponseEntity.ok(new EmployerProfileResponse(
                profile.getId(), profile.getCompanyName(), null,
                profile.getDescription(), profile.getIndustry(), profile.getWebsite(),
                profile.getVerificationStatus(), profile.getLogoUrl()
        ));
    }

    @PostMapping("/{employerId}/subscribe")
    public ResponseEntity<?> subscribe(@PathVariable Long employerId) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            if (user.getRole() != Role.ROLE_SEEKER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Ошибка: Только соискатели (студенты) могут подписываться на компании!");
            }

            SeekerProfile seeker = seekerProfileRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Профиль соискателя не найден"));

            EmployerProfile employer = employerProfileRepository.findById(employerId)
                    .orElseThrow(() -> new RuntimeException("Компания с ID " + employerId + " не найдена"));

            if (subscriptionRepository.existsBySeekerIdAndEmployerId(seeker.getId(), employer.getId())) {
                return ResponseEntity.badRequest().body("Вы уже подписаны на эту компанию");
            }

            subscriptionRepository.save(Subscription.builder()
                    .seeker(seeker)
                    .employer(employer)
                    .build());

            return ResponseEntity.ok("Вы успешно подписались на уведомления от " + employer.getCompanyName());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Ошибка бэкенда: " + e.getMessage());
        }
    }

    @DeleteMapping("/{employerId}/unsubscribe")
    public ResponseEntity<?> unsubscribe(@PathVariable Long employerId) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email).orElseThrow();

            Subscription subscription = subscriptionRepository.findBySeekerIdAndEmployerId(user.getId(), employerId)
                    .orElseThrow(() -> new RuntimeException("Вы не подписаны на эту компанию"));

            subscriptionRepository.delete(subscription);
            return ResponseEntity.ok("Вы отписались от уведомлений");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка: " + e.getMessage());
        }
    }
}

