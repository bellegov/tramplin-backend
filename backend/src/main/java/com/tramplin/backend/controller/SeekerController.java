package com.tramplin.backend.controller;

import com.tramplin.backend.dto.ApplicationResponse;
import com.tramplin.backend.dto.SeekerProfileDetailedResponse;
import com.tramplin.backend.dto.SeekerProfileResponse;
import com.tramplin.backend.dto.SeekerProfileUpdateRequest;
import com.tramplin.backend.model.SeekerProfile;
import com.tramplin.backend.model.User;
import com.tramplin.backend.repository.SeekerProfileRepository;
import com.tramplin.backend.repository.UserRepository;
import com.tramplin.backend.service.MinioService;
import com.tramplin.backend.service.SeekerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seekers")
@RequiredArgsConstructor
public class SeekerController {

    private final SeekerService seekerService;
    private final SeekerProfileRepository seekerProfileRepository;
    private final MinioService minioService;
    private final UserRepository userRepository;

    @PutMapping("/profile")
    public ResponseEntity<SeekerProfileResponse> updateProfile(@RequestBody SeekerProfileUpdateRequest request) {
        return ResponseEntity.ok(seekerService.updateProfile(request));
    }
    // В SeekerController.java добавь этот метод:

    @GetMapping("/profile")
    public ResponseEntity<SeekerProfileResponse> getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        SeekerProfile profile = seekerProfileRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Профиль не найден"));

        return ResponseEntity.ok(seekerService.mapToResponse(profile));
    }

    @PostMapping("/applications/{opportunityId}")
    public ResponseEntity<ApplicationResponse> apply(
            @PathVariable Long opportunityId,
            @RequestParam(required = false) String coverLetter) {
        return ResponseEntity.ok(seekerService.applyForOpportunity(opportunityId, coverLetter));
    }

    @GetMapping("/applications")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications() {
        return ResponseEntity.ok(seekerService.getMyApplications());
    }

    // Просмотр профиля ЛЮБОГО соискателя (с проверкой приватности)
    @GetMapping("/{id}")
    public ResponseEntity<SeekerProfileResponse> getPublicProfile(@PathVariable Long id) {
        SeekerProfile profile = seekerProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!profile.isPublic()) {
            // Тут можно добавить логику: если не публичный, то проверять, является ли запрашивающий другом
            throw new RuntimeException("Этот профиль скрыт настройками приватности");
        }

        return ResponseEntity.ok(seekerService.mapToResponse(profile));
        // (убедись, что метод mapToResponse в SeekerService сделан public)
    }

    @GetMapping("/{id}/detailed")
    public ResponseEntity<SeekerProfileDetailedResponse> getDetailed(@PathVariable Long id) {
        return ResponseEntity.ok(seekerService.getDetailedProfile(id));
    }

    @PostMapping(value = "/profile/avatar", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        // Вызываем сервис в контроллере напрямую (для скорости)
        String fileUrl = minioService.uploadFile(file);
        return ResponseEntity.ok(seekerService.updateAvatar(fileUrl)); // Метод updateAvatar мы сейчас напишем
    }
    @DeleteMapping("/profile/avatar")
    public ResponseEntity<String> deleteAvatar() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        SeekerProfile profile = seekerProfileRepository.findById(user.getId()).orElseThrow();

        profile.setAvatarUrl(null); // Просто стираем ссылку
        seekerProfileRepository.save(profile);

        return ResponseEntity.ok("Аватар удален");
    }
}