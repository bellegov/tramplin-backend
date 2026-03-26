package com.tramplin.backend.controller;

import com.tramplin.backend.dto.ApplicationResponse;
import com.tramplin.backend.dto.EmployerProfileResponse;
import com.tramplin.backend.dto.EmployerProfileUpdateRequest;
import com.tramplin.backend.dto.OpportunityResponse;
import com.tramplin.backend.model.ApplicationStatus;
import com.tramplin.backend.model.EmployerProfile;
import com.tramplin.backend.model.User;
import com.tramplin.backend.repository.EmployerProfileRepository;
import com.tramplin.backend.repository.UserRepository;
import com.tramplin.backend.service.EmployerService;
import com.tramplin.backend.service.MinioService;
import com.tramplin.backend.service.OpportunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employers/profile")
@RequiredArgsConstructor
public class EmployerController {

    private final EmployerService employerService;
    private final EmployerProfileRepository employerProfileRepository;
    private final MinioService minioService;
    private final UserRepository userRepository;
    private final OpportunityService opportunityService;

    @GetMapping
    public ResponseEntity<EmployerProfileResponse> getMyProfile() {
        return ResponseEntity.ok(employerService.getMyProfile());
    }

    @PutMapping
    public ResponseEntity<EmployerProfileResponse> updateProfile(@RequestBody EmployerProfileUpdateRequest request) {
        return ResponseEntity.ok(employerService.updateProfile(request));
    }

    @PostMapping(value = "/profile/logo", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadLogo(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        String fileUrl = minioService.uploadFile(file);
        return ResponseEntity.ok(employerService.updateLogo(fileUrl));
    }
    @GetMapping("/applications")
    public ResponseEntity<List<ApplicationResponse>> getApplications() {
        return ResponseEntity.ok(employerService.getApplicationsForMyOpportunities());
    }

    @PatchMapping("/applications/{applicationId}/status")
    public ResponseEntity<com.tramplin.backend.dto.ApplicationResponse> changeStatus(
            @PathVariable Long applicationId,
            @RequestParam ApplicationStatus status) {
        return ResponseEntity.ok(employerService.changeApplicationStatus(applicationId, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployerProfileResponse> getEmployerPublicProfile(@PathVariable Long id) {
        EmployerProfile profile = employerProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Компания не найдена"));

        // Отдаем DTO (пароли и лишнее не светятся)
        return ResponseEntity.ok(new EmployerProfileResponse(
                profile.getId(), profile.getCompanyName(), null, // ИНН можно скрыть в публичке
                profile.getDescription(), profile.getIndustry(), profile.getWebsite(), profile.getVerificationStatus(),profile.getLogoUrl()));
    }
    @PostMapping("/{employerId}/subscribe")
    public ResponseEntity<String> subscribe(@PathVariable Long employerId) {
        // Логика: находим текущего соискателя и сохраняем связь в SubscriptionRepository
        return ResponseEntity.ok("Вы подписались на уведомления компании");
    }

    @DeleteMapping("/profile/logo")
    public ResponseEntity<String> deleteLogo() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        EmployerProfile profile = employerProfileRepository.findById(user.getId()).orElseThrow();

        profile.setLogoUrl(null); // Стираем ссылку
        employerProfileRepository.save(profile);

        return ResponseEntity.ok("Логотип удален");
    }

    @GetMapping("/my-opportunities")
    public ResponseEntity<List<OpportunityResponse>> getMyOpportunities() {
        return ResponseEntity.ok(opportunityService.getMyOpportunities());
    }

}