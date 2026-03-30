package com.tramplin.backend.controller;

import com.tramplin.backend.dto.OpportunityCreateRequest;
import com.tramplin.backend.dto.OpportunityDetailedResponse;
import com.tramplin.backend.dto.OpportunityResponse;
import com.tramplin.backend.model.Opportunity;
import com.tramplin.backend.model.OpportunityStatus;
import com.tramplin.backend.model.User;
import com.tramplin.backend.model.WorkFormat;
import com.tramplin.backend.repository.OpportunityRepository;
import com.tramplin.backend.repository.UserRepository;
import com.tramplin.backend.service.MinioService;
import com.tramplin.backend.service.OpportunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/opportunities")
@RequiredArgsConstructor
public class OpportunityController {

    private final OpportunityService opportunityService;
    private final MinioService minioService;
    private final UserRepository userRepository;
    private final OpportunityRepository opportunityRepository;



    // Этот эндпоинт только для авторизованных верифицированных работодателей
    @PostMapping
    public ResponseEntity<OpportunityResponse> create(@RequestBody OpportunityCreateRequest request) {
        return ResponseEntity.ok(opportunityService.createOpportunity(request));
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<String> deleteOpportunityImage(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Opportunity opp = opportunityRepository.findById(id).orElseThrow();

        // Проверка, что удаляет владелец (работодатель)
        if (!opp.getEmployer().getId().equals(user.getId())) {
            throw new RuntimeException("Нет прав на удаление обложки");
        }

        opp.setImageUrl(null); // Стираем ссылку
        opportunityRepository.save(opp);

        return ResponseEntity.ok("Обложка вакансии удалена");
    }

    @GetMapping
    public ResponseEntity<List<OpportunityResponse>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) WorkFormat format,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Integer minSalary) {

        // Если параметров нет, метод в репозитории отработает корректно (выдаст всё)
        return ResponseEntity.ok(opportunityService.searchOpportunities(keyword,city, format, tag, minSalary));
    }

    // Получить детальную инфу (ДЛЯ МОДАЛКИ)
    @GetMapping("/{id}")
    public ResponseEntity<OpportunityDetailedResponse> getDetailed(@PathVariable Long id) {
        return ResponseEntity.ok(opportunityService.getDetailedById(id));
    }
    // Загрузить обложку вакансии
    @PostMapping(value = "/{id}/image", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        String fileUrl = minioService.uploadFile(file);
        return ResponseEntity.ok(opportunityService.uploadImage(id, fileUrl));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OpportunityResponse> update(
            @PathVariable Long id,
            @RequestBody OpportunityCreateRequest request) {
        return ResponseEntity.ok(opportunityService.updateOpportunity(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<com.tramplin.backend.dto.OpportunityResponse> changeStatus(
            @PathVariable Long id,
            @RequestParam OpportunityStatus status) {

        return ResponseEntity.ok(opportunityService.changeOpportunityStatus(id, status));
    }

}