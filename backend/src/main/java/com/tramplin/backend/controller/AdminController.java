package com.tramplin.backend.controller;

import com.tramplin.backend.dto.CreateCuratorRequest;
import com.tramplin.backend.dto.EmployerProfileResponse;
import com.tramplin.backend.model.*;
import com.tramplin.backend.repository.EmployerProfileRepository;
import com.tramplin.backend.repository.OpportunityRepository;
import com.tramplin.backend.repository.SeekerProfileRepository;
import com.tramplin.backend.service.AuthService;
import com.tramplin.backend.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final com.tramplin.backend.repository.UserRepository userRepository;
    private final MinioService minioService;
    private final SeekerProfileRepository seekerProfileRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final OpportunityRepository opportunityRepository;
    private final AuthService authService;

    // 1. Куратор получает список всех компаний, ожидающих проверку
    @GetMapping("/employers/pending")
    public ResponseEntity<List<EmployerProfileResponse>> getPendingEmployers() {
        List<EmployerProfileResponse> pendingList = employerProfileRepository.findAll().stream()
                .filter(p -> p.getVerificationStatus() == VerificationStatus.PENDING)
                .map(p -> new EmployerProfileResponse(
                        p.getId(), p.getCompanyName(), p.getInn(), p.getDescription(),
                        p.getIndustry(), p.getWebsite(), p.getVerificationStatus(), p.getLogoUrl()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(pendingList);
    }

    // 2. Куратор одобряет компанию (Верификация)
    @PostMapping("/employers/{id}/verify")
    public ResponseEntity<String> verifyEmployer(@PathVariable Long id) {
        EmployerProfile profile = employerProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Компания не найдена"));

        profile.setVerificationStatus(VerificationStatus.VERIFIED);
        employerProfileRepository.save(profile);

        return ResponseEntity.ok("Компания " + profile.getCompanyName() + " успешно верифицирована!");
    }

    // 3. Куратор отклоняет заявку
    @PostMapping("/employers/{id}/reject")
    public ResponseEntity<String> rejectEmployer(@PathVariable Long id) {
        EmployerProfile profile = employerProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Компания не найдена"));

        profile.setVerificationStatus(VerificationStatus.REJECTED);
        employerProfileRepository.save(profile);

        return ResponseEntity.ok("Заявка компании отклонена.");
    }

    // 1. Получить вообще всех пользователей (для управления)
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }



    // 3. Редактировать ЛЮБУЮ вакансию (модерация контента)
    @PutMapping("/opportunities/{id}")
    public ResponseEntity<Opportunity> updateOpportunityAdmin(@PathVariable Long id, @RequestBody Opportunity updated) {
        Opportunity existing = opportunityRepository.findById(id).orElseThrow();

        // Обновляем поля
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());

        // ЗАМЕНИЛИ ЭТО:
        // existing.setActive(updated.isActive());

        // НА ЭТО:
        existing.setStatus(updated.getStatus());

        // Также можем обновить зарплату и другие поля, если нужно
        existing.setSalary(updated.getSalary());
        existing.setCity(updated.getCity());
        existing.setWorkFormat(updated.getWorkFormat());

        return ResponseEntity.ok(opportunityRepository.save(existing));
    }

    // 4. Удалить вакансию
    @DeleteMapping("/opportunities/{id}")
    public ResponseEntity<Void> deleteOpportunity(@PathVariable Long id) {
        opportunityRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // 1. Редактировать профиль Работодателя (любого)
    @PutMapping("/employers/{id}")
    public ResponseEntity<EmployerProfileResponse> updateEmployerAdmin(
            @PathVariable Long id,
            @RequestBody com.tramplin.backend.dto.EmployerProfileUpdateRequest request) {

        EmployerProfile profile = employerProfileRepository.findById(id).orElseThrow();
        profile.setCompanyName(request.companyName());
        profile.setInn(request.inn());
        profile.setDescription(request.description());
        profile.setIndustry(request.industry());
        profile.setWebsite(request.website());

        EmployerProfile saved = employerProfileRepository.save(profile);
        return ResponseEntity.ok(new EmployerProfileResponse(
                saved.getId(), saved.getCompanyName(), saved.getInn(),
                saved.getDescription(), saved.getIndustry(), saved.getWebsite(), saved.getVerificationStatus(),saved.getLogoUrl()));
    }

    // 2. Редактировать профиль Соискателя (любого)
    @PutMapping("/seekers/{id}")
    public ResponseEntity<com.tramplin.backend.dto.SeekerProfileResponse> updateSeekerAdmin(
            @PathVariable Long id,
            @RequestBody com.tramplin.backend.dto.SeekerProfileUpdateRequest request) {

        SeekerProfile profile = seekerProfileRepository.findById(id).orElseThrow();
        profile.setUniversity(request.university());
        profile.setGraduationYear(request.graduationYear());
        profile.setResumeText(request.resumeText());
        profile.setPortfolioLinks(request.portfolioLinks());
        profile.setPublic(request.isPublic());

        SeekerProfile saved = seekerProfileRepository.save(profile);
        return ResponseEntity.ok(new com.tramplin.backend.dto.SeekerProfileResponse(
                saved.getId(), saved.getUser().getDisplayName(), saved.getUser().getEmail(),
                saved.getUniversity(), saved.getGraduationYear(), saved.getResumeText(),
                saved.getPortfolioLinks(), saved.isPublic(), saved.getAvatarUrl()));
    }


    // 1. Принудительно сменить аватар соискателя
    @PostMapping(value = "/seekers/{id}/avatar", consumes = "multipart/form-data")
    public ResponseEntity<String> setSeekerAvatar(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {

        String fileUrl = minioService.uploadFile(file);
        SeekerProfile profile = seekerProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Профиль соискателя не найден"));

        profile.setAvatarUrl(fileUrl);
        seekerProfileRepository.save(profile);

        return ResponseEntity.ok(fileUrl);
    }

    // 2. Принудительно сменить логотип работодателя
    @PostMapping(value = "/employers/{id}/logo", consumes = "multipart/form-data")
    public ResponseEntity<String> setEmployerLogo(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {

        String fileUrl = minioService.uploadFile(file);
        EmployerProfile profile = employerProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Профиль работодателя не найден"));

        profile.setLogoUrl(fileUrl);
        employerProfileRepository.save(profile);

        return ResponseEntity.ok(fileUrl);
    }

    // 3. Принудительно сменить обложку вакансии
    @PostMapping(value = "/opportunities/{id}/image", consumes = "multipart/form-data")
    public ResponseEntity<String> setOpportunityImage(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {

        String fileUrl = minioService.uploadFile(file);
        Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Вакансия не найдена"));

        opportunity.setImageUrl(fileUrl);
        opportunityRepository.save(opportunity);

        return ResponseEntity.ok(fileUrl);
    }

    /**
     * Удаляет аватар соискателя по его ID.
     * Только для администраторов.
     */
    @DeleteMapping("/seekers/{id}/avatar")
    public ResponseEntity<String> deleteSeekerAvatar(@PathVariable Long id) {
        SeekerProfile profile = seekerProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Профиль соискателя с ID " + id + " не найден"));

        profile.setAvatarUrl(null);
        seekerProfileRepository.save(profile);

        return ResponseEntity.ok("Аватар соискателя с ID " + id + " успешно удален");
    }

    /**
     * Удаляет логотип работодателя по его ID.
     * Только для администраторов.
     */
    @DeleteMapping("/employers/{id}/logo")
    public ResponseEntity<String> deleteEmployerLogo(@PathVariable Long id) {
        EmployerProfile profile = employerProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Профиль работодателя с ID " + id + " не найден"));

        profile.setLogoUrl(null);
        employerProfileRepository.save(profile);

        return ResponseEntity.ok("Логотип работодателя с ID " + id + " успешно удален");
    }

    /**
     * Удаляет обложку вакансии по её ID.
     * Только для администраторов.
     */
    @DeleteMapping("/opportunities/{id}/image")
    public ResponseEntity<String> deleteOpportunityImage(@PathVariable Long id) {
       Opportunity opportunity = opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Вакансия с ID " + id + " не найдена"));

        opportunity.setImageUrl(null);
        opportunityRepository.save(opportunity);

        return ResponseEntity.ok("Обложка вакансии с ID " + id + " успешно удалена");
    }

    @PostMapping("/create-curator")
    public ResponseEntity<String> createCurator(@RequestBody CreateCuratorRequest request) {
        authService.createCurator(request);
        return ResponseEntity.ok("Новый куратор с email: " + request.email() + " успешно создан.");
    }

    // 1. Получить список ВООБЩЕ ВСЕХ соискателей (студентов)
    @GetMapping("/seekers")
    public ResponseEntity<List<com.tramplin.backend.dto.SeekerProfileResponse>> getAllSeekers() {
        return ResponseEntity.ok(seekerProfileRepository.findAll().stream()
                .map(p -> new com.tramplin.backend.dto.SeekerProfileResponse(
                        p.getId(), p.getUser().getDisplayName(), p.getUser().getEmail(),
                        p.getUniversity(), p.getGraduationYear(), p.getResumeText(),
                        p.getPortfolioLinks(), p.isPublic(), p.getAvatarUrl()))
                .collect(java.util.stream.Collectors.toList()));
    }

    // 2. Получить список ВООБЩЕ ВСЕХ работодателей (компаний)
    @GetMapping("/employers")
    public ResponseEntity<List<com.tramplin.backend.dto.EmployerProfileResponse>> getAllEmployers() {
        return ResponseEntity.ok(employerProfileRepository.findAll().stream()
                .map(p -> new com.tramplin.backend.dto.EmployerProfileResponse(
                        p.getId(), p.getCompanyName(), p.getInn(), p.getDescription(),
                        p.getIndustry(), p.getWebsite(), p.getVerificationStatus(), p.getLogoUrl()))
                .collect(java.util.stream.Collectors.toList()));
    }

    // 3. Удалить пользователя (Бан-хаммер)
    @DeleteMapping("/users/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Пользователь с ID " + id + " не найден");
        }

        // Удаляем юзера (каскадом удалятся профили, если настроено в JPA,
        // либо удаляем вручную профили перед этим)
        userRepository.deleteById(id);

        return ResponseEntity.ok("Пользователь с ID " + id + " и все его данные удалены из системы навсегда.");
    }

}